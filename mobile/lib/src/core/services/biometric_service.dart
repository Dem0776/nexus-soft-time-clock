import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:local_auth/local_auth.dart';

/// Autenticación biométrica local (huella/rostro) antes de registrar asistencia (HU-14).
/// La política de si es obligatoria vive en el backend (por centro); aquí se intenta de forma
/// oportunista y se reporta el resultado en `biometricVerified` para que el servidor decida.
class BiometricService {
  final LocalAuthentication _auth = LocalAuthentication();

  /// Indica si el dispositivo puede realizar autenticación local.
  Future<bool> isAvailable() async {
    try {
      return await _auth.isDeviceSupported() && await _auth.canCheckBiometrics;
    } catch (_) {
      return false;
    }
  }

  /// Solicita autenticación local. Devuelve true solo si el usuario se autenticó con éxito.
  Future<bool> authenticate(String reason) async {
    try {
      if (!await isAvailable()) {
        return false;
      }
      return await _auth.authenticate(
        localizedReason: reason,
        options: const AuthenticationOptions(biometricOnly: false, stickyAuth: true),
      );
    } catch (_) {
      return false;
    }
  }
}

final biometricServiceProvider = Provider<BiometricService>((ref) => BiometricService());
