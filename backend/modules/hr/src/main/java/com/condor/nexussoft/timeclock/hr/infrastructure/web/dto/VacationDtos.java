package com.condor.nexussoft.timeclock.hr.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** DTOs de vacaciones (política y solicitudes). */
public final class VacationDtos {

    private VacationDtos() {}

    public record VacationTierDto(@NotNull Integer year, @NotNull Integer days) {}

    public record VacationPolicyDto(
            @NotNull List<VacationTierDto> tiers,
            @NotNull String beyondMode,        // "FLAT" | "INCREMENT"
            @NotNull Integer beyondIncrementDays,
            @NotNull Integer beyondEveryYears,
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
