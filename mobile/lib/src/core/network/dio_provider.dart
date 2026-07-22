import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../config/app_config.dart';
import '../storage/secure_token_store.dart';

/// Cliente Dio con interceptor que adjunta el Bearer JWT. El refresh 401 se
/// implementa junto con el registro de asistencia (offline-first) en iteraciones futuras.
final dioProvider = Provider<Dio>((ref) {
  final store = ref.read(secureTokenStoreProvider);
  final dio = Dio(BaseOptions(
    baseUrl: AppConfig.apiBaseUrl,
    connectTimeout: const Duration(seconds: 15),
    receiveTimeout: const Duration(seconds: 15),
  ));

  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) async {
      final path = options.path;
      final isAuthEndpoint = path.contains('/auth/login') || path.contains('/auth/refresh');
      if (!isAuthEndpoint) {
        final token = await store.accessToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
      }
      handler.next(options);
    },
  ));

  return dio;
});
