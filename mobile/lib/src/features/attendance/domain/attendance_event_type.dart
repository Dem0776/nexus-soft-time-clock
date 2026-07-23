/// Tipo de evento configurado por la empresa (HU-12 CA1). El backend devuelve los 5 tipos;
/// la app usa los intermedios habilitados para construir su UI.
class AttendanceEventTypeSetting {
  const AttendanceEventTypeSetting({
    required this.eventType,
    required this.enabled,
    required this.label,
  });

  final String eventType;
  final bool enabled;
  final String label;

  /// ENTRADA y SALIDA son núcleo (botones fijos); el resto son eventos intermedios.
  bool get isCore => eventType == 'ENTRADA' || eventType == 'SALIDA';

  factory AttendanceEventTypeSetting.fromJson(Map<String, dynamic> json) => AttendanceEventTypeSetting(
        eventType: json['eventType'] as String,
        enabled: json['enabled'] as bool? ?? true,
        label: (json['label'] as String?) ?? json['eventType'] as String,
      );
}
