package com.condor.nexussoft.timeclock.hr.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** DTOs de vacaciones (política y solicitudes). */
public final class VacationDtos {

    private VacationDtos() {}

    public record VacationPolicyDto(
            @NotNull Integer daysPerYear,
            boolean requireApproval,
            boolean countBusinessDaysOnly
    ) {}

    public record VacationRequestResponse(
            String id,
            String userId,
            String userName,
            String employeeCode,
            LocalDate startDate,
            LocalDate endDate,
            int days,
            String reason,
            String status,
            String resolutionNote,
            String resolvedBy,
            OffsetDateTime resolvedAt,
            OffsetDateTime createdAt
    ) {}

    public record CreateVacationRequest(
            String userId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            String reason
    ) {}

    public record ResolveVacationRequest(
            @NotNull String resolution,   // "APPROVED" | "REJECTED"
            String note
    ) {}
}
