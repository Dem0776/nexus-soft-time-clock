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
    );

    final db = ref.read(appDatabaseProvider);
    await db.enqueue(op.operationUuid, jsonEncode(op.toJson()));
    await ref.read(attendanceSyncServiceProvider).syncPending();
    await _refreshCount();

    state = state.copyWith(busy: false, message: 'Registro guardado y sincronizado.');
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
