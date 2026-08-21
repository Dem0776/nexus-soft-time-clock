import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/db/app_database.dart';
import '../../../core/network/dio_provider.dart';

/// Política de registro del centro, ya resuelta la herencia empresa → centro en el backend.
class SitePolicy {
  const SitePolicy({
    required this.requirePhoto,
    required this.requireBiometric,
    this.gpsAccuracyMaxM,
  });

  final bool requirePhoto;
  final bool requireBiometric;
  final int? gpsAccuracyMaxM;

  /// Sin política conocida no se exige nada: el servidor decidirá al recibir el registro.
  static const SitePolicy unknown = SitePolicy(requirePhoto: false, requireBiometric: false);
}

/// Consulta la política del centro y la cachea para poder aplicarla sin conexión.
///
/// La app la usa para pedir la foto *antes* de enviar; el veredicto sigue siendo del servidor
/// (RN-53). Si nunca se consultó este centro y no hay red, se ofrece la foto sin exigirla: es
/// preferible un rechazo explicable a bloquear un fichaje por una política que se desconoce.
class SitePolicyService {
  SitePolicyService(this._dio, this._db);

  final Dio _dio;
  final AppDatabase _db;

  Future<SitePolicy> forSite(String workSiteId) async {
    try {
      final res = await _dio.get<Map<String, dynamic>>('/attendance/site-policy/$workSiteId');
      final data = res.data!;
      final policy = SitePolicy(
        requirePhoto: data['requirePhoto'] as bool? ?? false,
        requireBiometric: data['requireBiometric'] as bool? ?? false,
        gpsAccuracyMaxM: data['gpsAccuracyMaxM'] as int?,
      );
      await _db.saveSitePolicy(
        workSiteId,
        policy.requirePhoto,
        policy.requireBiometric,
        policy.gpsAccuracyMaxM,
      );
      return policy;
    } on DioException {
      final cached = await _db.sitePolicy(workSiteId);
      if (cached == null) {
        return SitePolicy.unknown;
      }
      return SitePolicy(
        requirePhoto: cached.requirePhoto,
        requireBiometric: cached.requireBiometric,
        gpsAccuracyMaxM: cached.gpsAccuracyMaxM,
      );
    }
  }
}

final sitePolicyServiceProvider = Provider<SitePolicyService>(
  (ref) => SitePolicyService(ref.read(dioProvider), ref.read(appDatabaseProvider)),
);
