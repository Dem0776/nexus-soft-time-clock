/// Registro de asistencia devuelto por el historial propio (RF-05, HU-16).
///
/// Refleja el DTO `AttendanceSummaryResponse` del backend (`GET /attendance/me`): no trae
/// ids ni coordenadas, solo fecha/hora de servidor, tipo de evento, estado (+ motivo de
/// rechazo) y el nombre del centro de trabajo.
class AttendanceRecord {
  const AttendanceRecord({
    required this.eventType,
    required this.status,
    required this.serverTime,
    required this.workCenter,
    this.rejectionReason,
  });

  final String eventType;
  final String status;
  final DateTime serverTime;
  final String workCenter;
  final String? rejectionReason;

  bool get isAccepted => status == 'ACCEPTED';
  bool get isRejected => status == 'REJECTED';

  /// Etiqueta legible del tipo de evento.
  String get eventTypeLabel => _eventTypeLabels[eventType] ?? _prettify(eventType);

  /// Etiqueta legible del estado.
  String get statusLabel => _statusLabels[status] ?? _prettify(status);

  /// Motivo de rechazo legible (si aplica).
  String? get rejectionReasonLabel =>
      rejectionReason == null ? null : (_rejectionLabels[rejectionReason] ?? _prettify(rejectionReason!));

  factory AttendanceRecord.fromJson(Map<String, dynamic> json) => AttendanceRecord(
        eventType: json['eventType'] as String,
        status: json['status'] as String,
        serverTime: DateTime.parse(json['serverTime'] as String).toLocal(),
        workCenter: (json['workCenter'] as String?) ?? '—',
        rejectionReason: json['rejectionReason'] as String?,
      );
}

const Map<String, String> _eventTypeLabels = {
  'ENTRADA': 'Entrada',
  'SALIDA': 'Salida',
  'INICIO_DESCANSO': 'Inicio de descanso',
  'FIN_DESCANSO': 'Fin de descanso',
  'CAMBIO_SITIO': 'Cambio de sitio',
};

const Map<String, String> _statusLabels = {
  'ACCEPTED': 'Aceptado',
  'REJECTED': 'Rechazado',
  'PENDING_REVIEW': 'En revisión',
};

const Map<String, String> _rejectionLabels = {
  'INVALID_QR': 'QR inválido',
  'OUT_OF_GEOFENCE': 'Fuera de la geocerca',
  'LOW_GPS_ACCURACY': 'Precisión de GPS baja',
  'GPS_UNAVAILABLE': 'GPS no disponible',
  'OUT_OF_SCHEDULE': 'Fuera de horario',
  'FRAUD_MOCK_LOCATION': 'Ubicación simulada',
  'FRAUD_ROOTED_DEVICE': 'Dispositivo comprometido',
  'FRAUD_GPS_SPOOF_APP': 'App de suplantación de GPS',
  'REPLAY_DETECTED': 'Registro duplicado',
  'INVALID_SEQUENCE': 'Secuencia inválida',
  'UNTRUSTED_DEVICE': 'Dispositivo no confiable',
  'PHOTO_REQUIRED': 'Falta evidencia fotográfica',
  'BIOMETRIC_REQUIRED': 'Falta verificación biométrica',
  'EVENT_TYPE_DISABLED': 'Tipo de evento deshabilitado',
};

/// Convierte un código enum (`OUT_OF_GEOFENCE`) en texto legible (`Out of geofence`) como respaldo.
String _prettify(String code) {
  final words = code.toLowerCase().replaceAll('_', ' ').trim();
  return words.isEmpty ? code : words[0].toUpperCase() + words.substring(1);
}
