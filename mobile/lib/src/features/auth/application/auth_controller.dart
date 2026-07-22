import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/auth_repository.dart';
import '../domain/auth_models.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthState {
  const AuthState({required this.status, this.loading = false, this.error});

  final AuthStatus status;
  final bool loading;
  final String? error;

  bool get isAuthenticated => status == AuthStatus.authenticated;

  AuthState copyWith({AuthStatus? status, bool? loading, String? error}) => AuthState(
        status: status ?? this.status,
        loading: loading ?? this.loading,
        error: error,
      );
}

/// Controlador de sesión (Riverpod Notifier). Orquesta login/logout y expone el estado.
class AuthController extends Notifier<AuthState> {
  @override
  AuthState build() => const AuthState(status: AuthStatus.unauthenticated);

  Future<void> login(Credentials credentials) async {
    state = state.copyWith(loading: true);
    try {
      await ref.read(authRepositoryProvider).login(credentials);
      state = const AuthState(status: AuthStatus.authenticated);
    } catch (_) {
      state = const AuthState(
        status: AuthStatus.unauthenticated,
        error: 'Usuario o contraseña incorrectos',
      );
    }
  }

  Future<void> logout() async {
    await ref.read(authRepositoryProvider).logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }
}

final authControllerProvider =
    NotifierProvider<AuthController, AuthState>(AuthController.new);
