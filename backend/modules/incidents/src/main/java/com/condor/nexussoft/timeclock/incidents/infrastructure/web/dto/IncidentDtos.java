package com.condor.nexussoft.timeclock.incidents.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.incidents.domain.Incident;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class IncidentDtos {

    private IncidentDtos() {
    }

    public record ResolveIncidentRequest(
            @Pattern(regexp = "APPROVED|REJECTED|RESOLVED", message = "estado inválido") String status,
            @Size(max = 1000) String note) {
    }

    public record IncidentResponse(UUID id, UUID userId, String userName, String type, String status, String priority,
                                   LocalDate incidentDate, Instant createdAt, UUID relatedAttendanceId,
                                   String description, String resolutionNote, UUID resolvedBy, Instant resolvedAt) {
        public static IncidentResponse from(Incident i, String userName) {
            return new IncidentResponse(i.id(), i.userId(), userName, i.type().name(), i.status().name(), i.priority(),
                    i.incidentDate(), i.createdAt(), i.relatedAttendanceId(), i.description(),
                    i.resolutionNote(), i.resolvedBy(), i.resolvedAt());
        }
    }
}
