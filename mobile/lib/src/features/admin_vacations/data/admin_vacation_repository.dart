import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_provider.dart';
import '../domain/admin_vacation_models.dart';

/// Bandeja de vacaciones del tenant para administradores: listado paginado y
/// resolución (aprobar/rechazar). El backend protege ambos con `vacation:approve`.
class AdminVacationRepository {
  AdminVacationRepository(this._dio);
  final Dio _dio;

  Future<PagedVacations> list({int page = 0, int size = 20, String? status}) async {
    final res = await _dio.get<Map<String, dynamic>>(
      '/vacations/requests',
      queryParameters: {
        'page': page,
        'size': size,
        if (status != null && status.isNotEmpty) 'status': status,
      },
    );
    return PagedVacations.fromJson(res.data!);
  }

  Future<void> resolve(String id, {required bool approve, String? note}) async {
    await _dio.patch<Map<String, dynamic>>(
      '/vacations/requests/$id/resolve',
      data: {
        'resolution': approve ? 'APPROVED' : 'REJECTED',
        if (note != null && note.isNotEmpty) 'note': note,
      },
    );
  }
}

final adminVacationRepositoryProvider =
    Provider<AdminVacationRepository>((ref) => AdminVacationRepository(ref.read(dioProvider)));
