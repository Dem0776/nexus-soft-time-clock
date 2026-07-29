import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../features/auth/domain/auth_models.dart';

/// Almacenamiento seguro de tokens (Keychain/Keystore) — nunca en texto plano (RNF-08).
class SecureTokenStore {
  SecureTokenStore(this._storage);

  final FlutterSecureStorage _storage;
  static const _accessKey = 'nexus.accessToken';
  static const _refreshKey = 'nexus.refreshToken';

  // Caché en memoria: en algunos dispositivos el read-after-write del Keystore/Keychain
  // devuelve null momentáneamente, y el interceptor de Dio adjuntaría un request sin token
  // (401 en la primera llamada tras el login). El caché elimina esa carrera y evita un
  // read de almacenamiento seguro por cada petición.
  String? _accessCache;
  String? _refreshCache;

  Future<void> save(TokenResponse tokens) async {
    // Poblar el caché de forma síncrona, antes de los write asíncronos, para que
    // cualquier request inmediatamente posterior ya disponga del token.
    _accessCache = tokens.accessToken;
    _refreshCache = tokens.refreshToken;
    await _storage.write(key: _accessKey, value: tokens.accessToken);
    await _storage.write(key: _refreshKey, value: tokens.refreshToken);
  }

  Future<String?> accessToken() async => _accessCache ??= await _storage.read(key: _accessKey);

  Future<String?> refreshToken() async => _refreshCache ??= await _storage.read(key: _refreshKey);

  Future<void> clear() async {
    _accessCache = null;
    _refreshCache = null;
    await _storage.delete(key: _accessKey);
    await _storage.delete(key: _refreshKey);
  }
}

final secureTokenStoreProvider = Provider<SecureTokenStore>(
  (ref) => SecureTokenStore(const FlutterSecureStorage()),
);
