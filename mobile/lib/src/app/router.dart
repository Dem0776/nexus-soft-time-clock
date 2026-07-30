import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/admin_vacations/presentation/admin_vacations_screen.dart';
import '../features/attendance/presentation/attendance_history_screen.dart';
import '../features/attendance/presentation/attendance_screen.dart';
import '../features/auth/application/auth_controller.dart';
import '../features/auth/presentation/change_password_screen.dart';
import '../features/auth/presentation/login_screen.dart';
import '../features/home/presentation/home_screen.dart';
import '../features/profile/presentation/profile_screen.dart';
import '../features/vacations/presentation/vacations_screen.dart';

/// Router de la app (GoRouter) con redirect basado en la sesión: sin token válido se
/// fuerza /login; con sesión activa se evita volver a /login. Feature-first: cada
/// feature aporta sus rutas.
final routerProvider = Provider<GoRouter>((ref) {
  // Refresca el redirect del router cuando cambia la sesión (login/logout). Sin esto,
  // GoRouter solo re-evalúa el redirect ante navegación, no ante cambios de estado.
  final authListenable = ValueNotifier<bool>(ref.read(authControllerProvider).isAuthenticated);
  ref.listen(authControllerProvider, (previous, next) {
    authListenable.value = next.isAuthenticated;
  });
  ref.onDispose(authListenable.dispose);

  return GoRouter(
    initialLocation: '/',
    refreshListenable: authListenable,
    redirect: (context, state) {
      final authenticated = ref.read(authControllerProvider).isAuthenticated;
      final loggingIn = state.matchedLocation == '/login';
      if (!authenticated && !loggingIn) {
        return '/login';
      }
      if (authenticated && loggingIn) {
        return '/';
      }
      return null;
    },
    routes: [
      GoRoute(path: '/', name: 'home', builder: (context, state) => const HomeScreen()),
      GoRoute(path: '/login', name: 'login', builder: (context, state) => const LoginScreen()),
      GoRoute(path: '/attendance', name: 'attendance', builder: (context, state) => const AttendanceScreen()),
      GoRoute(
        path: '/attendance/history',
        name: 'attendanceHistory',
        builder: (context, state) => const AttendanceHistoryScreen(),
      ),
      GoRoute(path: '/profile', name: 'profile', builder: (context, state) => const ProfileScreen()),
      GoRoute(
        path: '/profile/change-password',
        name: 'changePassword',
        builder: (context, state) => const ChangePasswordScreen(),
      ),
      GoRoute(path: '/vacations', name: 'vacations', builder: (context, state) => const VacationsScreen()),
      GoRoute(
        path: '/admin/vacations',
        name: 'adminVacations',
        builder: (context, state) => const AdminVacationsScreen(),
      ),
    ],
  );
});
