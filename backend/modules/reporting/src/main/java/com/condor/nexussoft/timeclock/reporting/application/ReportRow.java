package com.condor.nexussoft.timeclock.reporting.application;

import java.time.Instant;
import java.util.UUID;

/** Fila del reporte de asistencia. */
public record ReportRow(
        Instant serverTime,
        UUID userId,
        String eventType,
        String status,
        String rejectionReason,
        double latitude,
        double longitude) {
}
