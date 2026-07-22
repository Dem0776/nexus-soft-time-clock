import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../features/auth/domain/auth_models.dart';

/// Almacenamiento seguro de tokens (Keychain/Keystore) — nunca en texto plano (RNF-08).
class SecureTokenStore {
  SecureTokenStore(this._storage);

  final FlutterSecureStorage _storage;
  static const _accessKey = 'nexus.accessToken';
  static const _refreshKey = 'nexus.refreshToken';

  Future<void> save(TokenResponse tokens) async {
    await _storage.write(key: _accessKey, value: tokens.accessToken);
    await _storage.write(key: _refreshKey, value: tokens.refreshToken);
  }

  Future<String?> accessToken() => _storage.read(key: _accessKey);

  Future<String?> refreshToken() => _storage.read(key: _refreshKey);

  Future<void> clear() async {
    await _storage.delete(key: _accessKey);
    await _storage.delete(key: _refreshKey);
  }
}

final secureTokenStoreProvider = Provider<SecureTokenStore>(
  (ref) => SecureTokenStore(const FlutterSecureStorage()),
);
