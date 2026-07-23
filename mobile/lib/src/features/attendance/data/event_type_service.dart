import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_provider.dart';
import '../domain/attendance_event_type.dart';

/// Obtiene del backend el catálogo de tipos de evento de la empresa (HU-12 CA1).
class EventTypeService {
  EventTypeService(this._dio);

  final Dio _dio;

  Future<List<AttendanceEventTypeSetting>> list() async {
    final res = await _dio.get<List<dynamic>>('/attendance/event-types');
    return (res.data ?? const [])
        .cast<Map<String, dynamic>>()
        .map(AttendanceEventTypeSetting.fromJson)
        .toList();
  }
}

final eventTypeServiceProvider =
    Provider<EventTypeService>((ref) => EventTypeService(ref.read(dioProvider)));

/// Tipos de evento INTERMEDIOS habilitados (para los botones dinámicos de la pantalla).
/// Si la consulta falla (offline), devuelve lista vacía: Entrada/Salida siempre están disponibles.
final enabledIntermediateEventTypesProvider =
    FutureProvider<List<AttendanceEventTypeSetting>>((ref) async {
  try {
    final all = await ref.read(eventTypeServiceProvider).list();
    return all.where((t) => !t.isCore && t.enabled).toList();
  } catch (_) {
    return const [];
  }
});
