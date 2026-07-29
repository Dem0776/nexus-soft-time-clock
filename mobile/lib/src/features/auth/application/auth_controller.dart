import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/db/app_database.dart';
import '../../admin_vacations/application/admin_vacations_controller.dart';
import '../../attendance/application/attendance_controller.dart';
import '../../attendance/data/attendance_sync_service.dart';
import '../../attendance/data/event_type_service.dart';
import '../../profile/data/profile_repository.dart';
import '../../vacations/data/vacation_repository.dart';
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
    // 1. Best-effort: intentar vaciar la cola offline mientras el token aún es válido,
    //    para no perder marcaciones que sí tienen red. Offline no debe romper el logout.
    try {
      await ref.read(attendanceSyncServiceProvider).syncPending();
    } catch (_) {
      // sin red: la cola se limpia igual en el paso 3
    }
    // 2. Cierra sesión en el backend y borra los tokens locales.
    await ref.read(authRepositoryProvider).logout();
    // 3. Descarta marcaciones residuales para que no se atribuyan al próximo usuario
    //    (la cola es global del dispositivo, sin scope de usuario).
    await ref.read(appDatabaseProvider).clearAll();
    // 4. Invalida todo el estado cacheado del usuario para que el próximo login refetchee.
    _clearUserScopedState();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  /// Invalida los providers con datos propios del colaborador. Sin esto, Riverpod conserva
  /// en caché la respuesta del usuario anterior y la muestra al siguiente que inicie sesión.
  void _clearUserScopedState() {
    ref.invalidate(meProvider);
    ref.invalidate(myProfileProvider);
    ref.invalidate(vacationSummaryProvider);
    ref.invalidate(myVacationsProvider);
    ref.invalidate(enabledIntermediateEventTypesProvider);
    ref.invalidate(attendanceControllerProvider);
    ref.invalidate(adminVacationsControllerProvider);
  }
}

final authControllerProvider =
    NotifierProvider<AuthController, AuthState>(AuthController.new);
