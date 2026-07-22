package com.condor.nexussoft.timeclock.organization.domain.port.in;

import com.condor.nexussoft.timeclock.organization.domain.Project;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

/** Administración de proyectos (RF-23), acotada al tenant. */
public interface ProjectManagementUseCase {

    Project create(UUID tenantId, ProjectCommands.CreateProjectCommand command);

    Project update(UUID tenantId, UUID id, ProjectCommands.UpdateProjectCommand command);

    Project get(UUID tenantId, UUID id);

    Paged<Project> list(UUID tenantId, int page, int size, String search);
}
