package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.organization.domain.Project;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(UUID id, String code, String name, String status,
                              LocalDate startsOn, LocalDate endsOn) {

    public static ProjectResponse from(Project p) {
        return new ProjectResponse(p.id(), p.code(), p.name(), p.status().name(), p.startsOn(), p.endsOn());
    }
}
