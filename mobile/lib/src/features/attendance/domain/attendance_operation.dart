/// Operación de registro creada en el dispositivo. Su JSON es el cuerpo de
/// RegisterAttendanceRequest del backend (idempotente por operationUuid).
class AttendanceOperation {
  const AttendanceOperation({
    required this.operationUuid,
    required this.workSiteId,
    required this.qrToken,
    required this.latitude,
    required this.longitude,
    required this.accuracyM,
    required this.eventType,
    required this.source,
    this.deviceId,
    this.deviceTimeEpochMs,
    this.mockLocation = false,
    this.rootedOrJailbroken = false,
    this.gpsSpoofApp = false,
    this.gpsDisabled = false,
    this.deviceTrusted = true,
    this.biometricVerified = false,
  });

  final String operationUuid;
  final String workSiteId;
  final String qrToken;
  final double latitude;
  final double longitude;
  final double accuracyM;
  final String eventType;
  final String source;
  final String? deviceId;
  final int? deviceTimeEpochMs;
  final bool mockLocation;
  final bool rootedOrJailbroken;
  final bool gpsSpoofApp;
  final bool gpsDisabled;
  final bool deviceTrusted;
  final bool biometricVerified;

  Map<String, dynamic> toJson() => {
        'operationUuid': operationUuid,
        'workSiteId': workSiteId,
        'qrToken': qrToken,
        'latitude': latitude,
        'longitude': longitude,
        'accuracyM': accuracyM,
        'eventType': eventType,
        'source': source,
        if (deviceId != null) 'deviceId': deviceId,
        if (deviceTimeEpochMs != null) 'deviceTimeEpochMs': deviceTimeEpochMs,
        'mockLocation': mockLocation,
        'rootedOrJailbroken': rootedOrJailbroken,
        'gpsSpoofApp': gpsSpoofApp,
        'gpsDisabled': gpsDisabled,
        'deviceTrusted': deviceTrusted,
        'biometricVerified': biometricVerified,
      };
}
