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

  /// Cambio de contraseña del propio usuario (RF-01). Requiere la contraseña actual.
  Future<void> changePassword(String currentPassword, String newPassword) async {
    await _dio.post<void>(
      '/auth/change-password',
      data: {'currentPassword': currentPassword, 'newPassword': newPassword},
    );
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

/// Sesión del usuario autenticado (roles/permisos vía GET /auth/me). Se usa para
/// condicionar la UI por rol (p. ej. la sección de administración en el Home).
final meProvider = FutureProvider<Me>((ref) => ref.read(authRepositoryProvider).me());
