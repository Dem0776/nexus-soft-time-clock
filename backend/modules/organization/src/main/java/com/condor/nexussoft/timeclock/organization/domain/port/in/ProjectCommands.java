package com.condor.nexussoft.timeclock.organization.domain.port.in;

import java.time.LocalDate;

public final class ProjectCommands {

    private ProjectCommands() {
    }

    public record CreateProjectCommand(String code, String name, LocalDate startsOn, LocalDate endsOn) {
    }

    public record UpdateProjectCommand(String name, String status, LocalDate startsOn, LocalDate endsOn) {
    }
}
