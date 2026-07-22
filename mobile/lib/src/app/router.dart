import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/attendance/presentation/attendance_screen.dart';
import '../features/auth/application/auth_controller.dart';
import '../features/auth/presentation/login_screen.dart';
import '../features/home/presentation/home_screen.dart';

/// Router de la app (GoRouter) con redirect basado en la sesión: sin token válido se
/// fuerza /login; con sesión activa se evita volver a /login. Feature-first: cada
/// feature aporta sus rutas.
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/',
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
    ],
  );
});
