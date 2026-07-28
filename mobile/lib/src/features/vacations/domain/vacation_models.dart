/// Resumen de vacaciones del colaborador (espejo de MeVacationSummaryResponse).
class VacationSummary {
  const VacationSummary({
    required this.yearsOfService,
    required this.entitledDays,
    required this.takenDays,
    required this.pendingDays,
    required this.availableDays,
  });

  final int yearsOfService;
  final int entitledDays;
  final int takenDays;
  final int pendingDays;
  final int availableDays;

  factory VacationSummary.fromJson(Map<String, dynamic> j) => VacationSummary(
        yearsOfService: (j['yearsOfService'] as num?)?.toInt() ?? 0,
        entitledDays: (j['entitledDays'] as num?)?.toInt() ?? 0,
        takenDays: (j['takenDays'] as num?)?.toInt() ?? 0,
        pendingDays: (j['pendingDays'] as num?)?.toInt() ?? 0,
        availableDays: (j['availableDays'] as num?)?.toInt() ?? 0,
      );
}

/// Una solicitud de vacaciones del colaborador (espejo de VacationRequestResponse).
class MyVacationRequest {
  const MyVacationRequest({
    required this.id,
    required this.startDate,
    required this.endDate,
    required this.days,
    required this.status,
    this.reason,
    this.resolutionNote,
  });

  final String id;
  final String startDate;
  final String endDate;
  final int days;
  final String status;
  final String? reason;
  final String? resolutionNote;

  factory MyVacationRequest.fromJson(Map<String, dynamic> j) => MyVacationRequest(
        id: j['id'] as String? ?? '',
        startDate: j['startDate'] as String? ?? '',
        endDate: j['endDate'] as String? ?? '',
        days: (j['days'] as num?)?.toInt() ?? 0,
        status: j['status'] as String? ?? 'PENDING',
        reason: j['reason'] as String?,
        resolutionNote: j['resolutionNote'] as String?,
      );
}
