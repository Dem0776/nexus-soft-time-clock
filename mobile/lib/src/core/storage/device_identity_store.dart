import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:uuid/uuid.dart';

/// Identidad estable del dispositivo para device binding (RF-28, RN-27).
///
/// Genera y persiste un identificador propio la primera vez (get-or-create) en el
/// almacenamiento seguro, y expone los metadatos (plataforma/modelo/OS) que el backend
/// usa al enrolar el dispositivo. El identificador vive mientras la app no se desinstale;
/// no depende de IDs de hardware (privacidad) sino de un UUID generado localmente.
class DeviceIdentity {
  const DeviceIdentity({
    required this.deviceId,
    required this.platform,
    this.model,
    this.osVersion,
  });

  final String deviceId;
  final String platform; // ANDROID | IOS | WEB
  final String? model;
  final String? osVersion;
}

class DeviceIdentityStore {
  DeviceIdentityStore(this._storage, this._deviceInfo);

  final FlutterSecureStorage _storage;
  final DeviceInfoPlugin _deviceInfo;
  static const _deviceIdKey = 'nexus.deviceId';
  static const _uuid = Uuid();

  DeviceIdentity? _cache;

  /// Devuelve la identidad del dispositivo, creándola la primera vez.
  Future<DeviceIdentity> current() async {
    if (_cache != null) return _cache!;

    var deviceId = await _storage.read(key: _deviceIdKey);
    if (deviceId == null || deviceId.isEmpty) {
      deviceId = _uuid.v4();
      await _storage.write(key: _deviceIdKey, value: deviceId);
    }

    final info = await _readInfo();
    return _cache = DeviceIdentity(
      deviceId: deviceId,
      platform: info.$1,
      model: info.$2,
      osVersion: info.$3,
    );
  }

  /// (platform, model, osVersion). Tolerante a fallos: si device_info no responde, igual
  /// se enrola con la plataforma del SO y sin modelo/OS.
  Future<(String, String?, String?)> _readInfo() async {
    try {
      if (Platform.isAndroid) {
        final a = await _deviceInfo.androidInfo;
        return ('ANDROID', '${a.manufacturer} ${a.model}'.trim(), 'Android ${a.version.release}');
      }
      if (Platform.isIOS) {
        final i = await _deviceInfo.iosInfo;
        return ('IOS', i.utsname.machine, '${i.systemName} ${i.systemVersion}');
      }
    } catch (_) {
      // Ignorado: el enrolamiento no debe fallar por metadatos opcionales.
    }
    final platform = Platform.isIOS ? 'IOS' : Platform.isAndroid ? 'ANDROID' : 'WEB';
    return (platform, null, null);
  }
}

final deviceIdentityStoreProvider = Provider<DeviceIdentityStore>(
  (ref) => DeviceIdentityStore(const FlutterSecureStorage(), DeviceInfoPlugin()),
);
