import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../core/network/dio_provider.dart';
import '../domain/attendance_record.dart';

/// Consulta el historial propio del colaborador (RF-05, HU-16) desde `GET /attendance/me`.
/// Filtra por rango de fechas (opcional) enviando `from`/`to` como `yyyy-MM-dd`.
class AttendanceHistoryService {
  AttendanceHistoryService(this._dio);

  final Dio _dio;

  static final DateFormat _dayFormat = DateFormat('yyyy-MM-dd');

  Future<List<AttendanceRecord>> myHistory({DateTime? from, DateTime? to, int limit = 100}) async {
    final res = await _dio.get<List<dynamic>>(
      '/attendance/me',
      queryParameters: {
        if (from != null) 'from': _dayFormat.format(from),
        if (to != null) 'to': _dayFormat.format(to),
        'limit': limit,
      },
    );
    return (res.data ?? const [])
        .cast<Map<String, dynamic>>()
        .map(AttendanceRecord.fromJson)
        .toList();
  }
}

final attendanceHistoryServiceProvider =
    Provider<AttendanceHistoryService>((ref) => AttendanceHistoryService(ref.read(dioProvider)));
