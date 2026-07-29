package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del registro: aceptado/rechazado, hora de servidor y banderas antifraude.
 * {@code minutesLate} es la tardanza sobre la tolerancia del turno (RN-16); 0 si es puntual o no aplica.
 */
public record AttendanceResult(
        UUID recordId,
        String status,
        String rejectionReason,
        Instant serverTime,
        Double distanceToSiteM,
        List<String> flags,
        int minutesLate) {
}
