package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import java.util.List;
import java.util.UUID;

/** Puerto de entrada del núcleo: registrar asistencia e historial propio (CU-02..CU-05). */
public interface RegisterAttendanceUseCase {

    AttendanceResult register(UUID tenantId, UUID userId, RegisterAttendanceCommand command);

    List<AttendanceSummary> history(UUID tenantId, UUID userId, int limit);
}
