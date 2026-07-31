import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Señal de "sesión terminada por el servidor" (refresh token inválido/expirado/reuso).
/// El interceptor de Dio la incrementa cuando el refresh falla; `AuthController` la escucha
/// y ejecuta la limpieza de sesión. Se usa un provider intermedio (sin dependencias) para
/// evitar un ciclo entre `dioProvider` y los providers de autenticación.
final sessionExpiredSignalProvider = StateProvider<int>((ref) => 0);
