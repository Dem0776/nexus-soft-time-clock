package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Resultado del registro: aceptado/rechazado, hora de servidor y banderas antifraude. */
public record AttendanceResult(
        UUID recordId,
        String status,
        String rejectionReason,
        Instant serverTime,
        Double distanceToSiteM,
        List<String> flags) {
}
