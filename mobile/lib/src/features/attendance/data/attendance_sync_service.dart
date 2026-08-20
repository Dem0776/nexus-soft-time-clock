import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/db/app_database.dart';
import '../../../core/network/dio_provider.dart';
import 'evidence_upload_service.dart';

/// Envía la cola local al backend por lotes y aplica el resultado autoritativo del
/// servidor a cada operación (RN-53, RN-54). Ante fallo de red, incrementa intentos
/// y deja PENDING para reintento con backoff (RN-52).
class AttendanceSyncService {
  AttendanceSyncService(this._dio, this._db, this._evidence);

  final Dio _dio;
  final AppDatabase _db;
  final EvidenceUploadService _evidence;

  Future<void> syncPending() async {
    final pending = await _db.pending();
    if (pending.isEmpty) {
      return;
    }

    // La evidencia viaja aparte del lote: primero se sube la foto y solo entonces la operación
    // puede enviarse con su referencia. Una subida fallida no arrastra al resto del lote.
    final sent = <String>[];
    final operations = <Map<String, dynamic>>[];
    for (final op in pending) {
      final payload = await _payloadWithEvidence(op);
      if (payload != null) {
        operations.add(payload);
        sent.add(op.operationUuid);
      }
    }
    if (operations.isEmpty) {
      return;
    }

    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/sync/attendance',
        data: {'operations': operations},
      );
      final results = (response.data!['results'] as List).cast<Map<String, dynamic>>();
      for (final r in results) {
        final uuid = r['operationUuid'] as String;
        final error = r['error'] as String?;
        final status = r['status'] as String?;
        if (error != null) {
          // Fallo transitorio por operación (RN-54): el servidor pide reintento.
          // Se mantiene PENDING e incrementa intentos; no se marca ERROR terminal
          // para no perder el registro fuera de la cola de sincronización (RN-52).
          await _db.incrementAttempts(uuid, error);
        } else if (status == 'ACCEPTED') {
          // Aceptado; si el servidor detectó retardo (RN-16) se anota en la nota para informarlo.
          final late = r['minutesLate'] as int?;
          await _db.markStatus(uuid, 'SYNCED', (late != null && late > 0) ? 'LATE:$late' : null);
          await _discardLocalPhoto(uuid);
        } else {
          await _db.markStatus(uuid, 'REJECTED', r['rejectionReason'] as String?);
          await _discardLocalPhoto(uuid);
        }
      }
    } on DioException catch (e) {
      // Fallo de red/servidor: no se pierde nada; se reintentará.
      for (final uuid in sent) {
        await _db.incrementAttempts(uuid, e.message ?? 'network');
      }
    }
  }

  /// Devuelve el payload de la operación listo para enviarse, subiendo antes su foto si hace falta.
  ///
  /// Devuelve `null` si la subida falla: esa operación se queda fuera del lote y se reintenta,
  /// porque enviarla sin evidencia haría que un centro con foto obligatoria la rechace en firme.
  Future<Map<String, dynamic>?> _payloadWithEvidence(PendingAttendanceOp op) async {
    final payload = jsonDecode(op.payload) as Map<String, dynamic>;

    final path = op.evidencePath;
    if (path == null || op.evidenceKey != null) {
      return payload; // sin foto, o ya subida en un intento anterior
    }

    final file = File(path);
    if (!file.existsSync()) {
      // El archivo se perdió (limpieza del sistema, borrado manual). Se envía sin evidencia y que
      // el servidor decida: si el centro la exige, responderá PHOTO_REQUIRED con su motivo.
      await _db.clearEvidencePath(op.operationUuid);
      return payload;
    }

    try {
      final uploaded = await _evidence.upload(
        file: file,
        workSiteId: payload['workSiteId'] as String,
        sha256: op.evidenceSha256 ?? '',
        sizeBytes: file.lengthSync(),
        contentType: 'image/jpeg',
      );
      if (uploaded == null) {
        return null;
      }
      await _db.markEvidenceUploaded(op.operationUuid, uploaded.bucket, uploaded.objectKey);
      payload['evidenceBucket'] = uploaded.bucket;
      payload['evidenceKey'] = uploaded.objectKey;
      if (op.evidenceSha256 != null) {
        payload['evidenceHash'] = op.evidenceSha256;
      }
      return payload;
    } on DioException catch (e) {
      await _db.incrementAttempts(op.operationUuid, 'evidencia: ${e.message ?? 'red'}');
      return null;
    }
  }

  /// Tras un veredicto definitivo el archivo local ya no hace falta.
  Future<void> _discardLocalPhoto(String operationUuid) async {
    final row = await _db.findByUuid(operationUuid);
    final path = row?.evidencePath;
    if (path == null) {
      return;
    }
    try {
      final file = File(path);
      if (file.existsSync()) {
        await file.delete();
      }
    } catch (_) {
      // Un archivo que no se puede borrar no debe romper la sincronización.
    }
    await _db.clearEvidencePath(operationUuid);
  }
}

final attendanceSyncServiceProvider = Provider<AttendanceSyncService>(
  (ref) => AttendanceSyncService(
    ref.read(dioProvider),
    ref.read(appDatabaseProvider),
    ref.read(evidenceUploadServiceProvider),
  ),
);
