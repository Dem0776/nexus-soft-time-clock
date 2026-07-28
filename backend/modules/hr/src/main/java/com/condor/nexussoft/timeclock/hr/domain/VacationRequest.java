package com.condor.nexussoft.timeclock.hr.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Solicitud de vacaciones y su resolución. */
public record VacationRequest(
        UUID id,
        UUID tenantId,
        UUID userId,
        LocalDate startDate,
        LocalDate endDate,
        int days,
        String reason,
        VacationStatus status,
        String resolutionNote,
        UUID resolvedBy,
        OffsetDateTime resolvedAt,
        OffsetDateTime createdAt
) {}
