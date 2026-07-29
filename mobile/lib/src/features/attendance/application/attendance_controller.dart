import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';

import '../../../core/db/app_database.dart';
import '../../../core/services/location_service.dart';
import '../data/attendance_sync_service.dart';
import '../domain/attendance_operation.dart';

class AttendanceUiState {
  const AttendanceUiState({this.busy = false, this.message, this.pendingCount = 0});

  final bool busy;
  final String? message;
  final int pendingCount;

  AttendanceUiState copyWith({bool? busy, String? message, int? pendingCount}) => AttendanceUiState(
        busy: busy ?? this.busy,
        message: message,
        pendingCount: pendingCount ?? this.pendingCount,
      );
}

/// Orquesta el registro offline-first: captura GPS → construye la operación (UUID) →
/// la encola localmente (nunca se pierde) → intenta sincronizar. La validación real
/// (geocerca, antifraude, hora) la resuelve el servidor (RN-53).
class AttendanceController extends Notifier<AttendanceUiState> {
  static const _uuid = Uuid();

  @override
  AttendanceUiState build() {
    _refreshCount();
    return const AttendanceUiState();
  }

  Future<void> register({
    required String eventType,
    required String workSiteId,
    required String qrToken,
    bool biometricVerified = false,
  }) async {
    state = state.copyWith(busy: true, message: null);

    final gpsEnabled = await ref.read(locationServiceProvider).isGpsEnabled();
    final position = await ref.read(locationServiceProvider).current();
    if (position == null) {
      state = state.copyWith(busy: false, message: 'No se pudo obtener la ubicación GPS.');
      return;
    }

    final op = AttendanceOperation(
      operationUuid: _uuid.v4(),
      workSiteId: workSiteId,
      qrToken: qrToken,
      latitude: position.latitude,
      longitude: position.longitude,
      accuracyM: position.accuracy,
      eventType: eventType,
      source: 'ONLINE',
      deviceTimeEpochMs: DateTime.now().millisecondsSinceEpoch,
      mockLocation: position.isMocked,
      gpsDisabled: !gpsEnabled,
      biometricVerified: biometricVerified,
    );

    final db = ref.read(appDatabaseProvider);
    await db.enqueue(op.operationUuid, jsonEncode(op.toJson()));
    await ref.read(attendanceSyncServiceProvider).syncPending();
    await _refreshCount();

    // El mensaje refleja el veredicto autoritativo del servidor aplicado por el sync a la
    // fila local (HU-10 CA3, HU-15 CA4): aceptado, rechazado con motivo, o aún pendiente si
    // no hubo red. Se fija DESPUÉS de _refreshCount porque copyWith resetea message en cada llamada.
    final row = await db.findByUuid(op.operationUuid);
    state = state.copyWith(busy: false, message: _messageFor(row, position.accuracy));
  }

  /// Traduce el estado final de la operación (tras sincronizar) a un mensaje para el colaborador.
  /// [accuracyM] es la precisión del fix enviado; se muestra en el rechazo por GPS para dar
  /// contexto accionable (saber cuán lejos quedó del umbral) y como dato de diagnóstico.
  String _messageFor(PendingAttendanceOp? row, double accuracyM) {
    switch (row?.status) {
      case 'SYNCED':
        // Para SYNCED, lastError se reutiliza como nota del servidor: 'LATE:<min>' si hubo retardo.
        final note = row?.lastError;
        if (note != null && note.startsWith('LATE:')) {
          return 'Registro aceptado (retardo de ${note.substring(5)} min).';
        }
        return 'Registro aceptado.';
      case 'REJECTED':
        final reason = row?.lastError;
        if (reason == 'LOW_GPS_ACCURACY') {
          return 'Registro rechazado: precisión de GPS baja (±${accuracyM.round()} m). '
              'Muévete a un lugar abierto (sal al exterior) y vuelve a intentarlo.';
        }
        return 'Registro rechazado: ${_rejectionLabel(reason)}.';
      default:
        // PENDING/ERROR o fila ausente: no se perdió, se reintentará al recuperar conexión.
        return 'Registro guardado. Se sincronizará cuando haya conexión.';
    }
  }

  /// Motivo de rechazo (código del backend, RejectionReason) a texto en español.
  String _rejectionLabel(String? reason) {
    switch (reason) {
      case 'INVALID_QR':
        return 'QR inválido o expirado';
      case 'OUT_OF_GEOFENCE':
        return 'fuera del área permitida';
      case 'LOW_GPS_ACCURACY':
        return 'precisión de GPS baja';
      case 'GPS_UNAVAILABLE':
        return 'GPS no disponible';
      case 'OUT_OF_SCHEDULE':
        return 'fuera del horario del turno';
      case 'FRAUD_MOCK_LOCATION':
        return 'ubicación simulada detectada';
      case 'FRAUD_ROOTED_DEVICE':
        return 'dispositivo comprometido (root/jailbreak)';
      case 'FRAUD_GPS_SPOOF_APP':
        return 'app de falsificación de GPS detectada';
      case 'REPLAY_DETECTED':
        return 'QR ya utilizado';
      case 'INVALID_SEQUENCE':
        return 'secuencia de marcaciones inválida';
      case 'UNTRUSTED_DEVICE':
        return 'dispositivo no confiable';
      case 'PHOTO_REQUIRED':
        return 'se requiere evidencia fotográfica';
      case 'BIOMETRIC_REQUIRED':
        return 'se requiere verificación biométrica';
      case 'EVENT_TYPE_DISABLED':
        return 'tipo de evento no habilitado';
      default:
        return reason ?? 'motivo desconocido';
    }
  }

  Future<void> syncNow() async {
    state = state.copyWith(busy: true);
    await ref.read(attendanceSyncServiceProvider).syncPending();
    await _refreshCount();
    state = state.copyWith(busy: false, message: 'Sincronización completada.');
  }

  Future<void> _refreshCount() async {
    final count = await ref.read(appDatabaseProvider).pendingCount();
    state = state.copyWith(pendingCount: count);
  }
}

final attendanceControllerProvider =
    NotifierProvider<AttendanceController, AttendanceUiState>(AttendanceController.new);
