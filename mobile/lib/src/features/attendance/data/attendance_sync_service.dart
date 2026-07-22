import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/db/app_database.dart';
import '../../../core/network/dio_provider.dart';

/// Envía la cola local al backend por lotes y aplica el resultado autoritativo del
/// servidor a cada operación (RN-53, RN-54). Ante fallo de red, incrementa intentos
/// y deja PENDING para reintento con backoff (RN-52).
class AttendanceSyncService {
  AttendanceSyncService(this._dio, this._db);

  final Dio _dio;
  final AppDatabase _db;

  Future<void> syncPending() async {
    final pending = await _db.pending();
    if (pending.isEmpty) {
      return;
    }

    final operations = pending.map((op) => jsonDecode(op.payload) as Map<String, dynamic>).toList();

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
          await _db.markStatus(uuid, 'SYNCED', null);
        } else {
          await _db.markStatus(uuid, 'REJECTED', r['rejectionReason'] as String?);
        }
      }
    } on DioException catch (e) {
      // Fallo de red/servidor: no se pierde nada; se reintentará.
      for (final op in pending) {
        await _db.incrementAttempts(op.operationUuid, e.message ?? 'network');
      }
    }
  }
}

final attendanceSyncServiceProvider = Provider<AttendanceSyncService>(
  (ref) => AttendanceSyncService(ref.read(dioProvider), ref.read(appDatabaseProvider)),
);
