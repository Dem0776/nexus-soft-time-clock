import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_provider.dart';
import '../../../core/storage/secure_token_store.dart';
import '../domain/auth_models.dart';

/// Acceso a los endpoints de autenticación de la API (RF-01).
class AuthRepository {
  AuthRepository(this._dio, this._store);

  final Dio _dio;
  final SecureTokenStore _store;

  Future<TokenResponse> login(Credentials credentials) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: credentials.toJson(),
    );
    final tokens = TokenResponse.fromJson(response.data!);
    await _store.save(tokens);
    return tokens;
  }

  Future<Me> me() async {
    final response = await _dio.get<Map<String, dynamic>>('/auth/me');
    return Me.fromJson(response.data!);
  }

  Future<void> logout() async {
    final refreshToken = await _store.refreshToken();
    if (refreshToken != null) {
      try {
        await _dio.post<void>('/auth/logout', data: {'refreshToken': refreshToken});
      } catch (_) {
        // logout es best-effort: siempre limpiamos el almacenamiento local
      }
    }
    await _store.clear();
  }
}

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepository(ref.read(dioProvider), ref.read(secureTokenStoreProvider)),
);
