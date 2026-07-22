package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import java.time.Instant;
import java.util.UUID;

/** Resumen de un registro para el historial del colaborador (RF-05). */
public record AttendanceSummary(
        UUID id,
        String eventType,
        String status,
        String rejectionReason,
        Instant serverTime,
        double latitude,
        double longitude) {
}
