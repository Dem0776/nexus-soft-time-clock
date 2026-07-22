package com.condor.nexussoft.timeclock.scheduling.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.scheduling.domain.ShiftAssignment;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/** DTOs de asignación de turnos. */
public final class AssignmentDtos {

    private AssignmentDtos() {
    }

    public record AssignmentRequest(
            @NotNull UUID userId,
            @NotNull UUID shiftId,
            UUID workSiteId,
            @NotNull LocalDate validFrom,
            LocalDate validTo) {
    }

    public record AssignmentResponse(UUID id, UUID userId, UUID shiftId, UUID workSiteId,
                                     LocalDate validFrom, LocalDate validTo) {
        public static AssignmentResponse from(ShiftAssignment a) {
            return new AssignmentResponse(a.id(), a.userId(), a.shiftId(), a.workSiteId(),
                    a.validFrom(), a.validTo());
        }
    }
}
