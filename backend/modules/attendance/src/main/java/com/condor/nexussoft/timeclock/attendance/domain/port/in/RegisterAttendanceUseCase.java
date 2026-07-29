package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada del núcleo: registrar asistencia e historial propio (CU-02..CU-05). */
public interface RegisterAttendanceUseCase {

    AttendanceResult register(UUID tenantId, UUID userId, RegisterAttendanceCommand command);

    /**
     * Historial del colaborador acotado por rango de fechas (HU-16 CA1). {@code from}/{@code to}
     * son opcionales (nulos = sin cota por ese extremo); {@code to} es inclusivo por día.
     */
    List<AttendanceSummary> history(UUID tenantId, UUID userId, Instant from, Instant to, int limit);
}
