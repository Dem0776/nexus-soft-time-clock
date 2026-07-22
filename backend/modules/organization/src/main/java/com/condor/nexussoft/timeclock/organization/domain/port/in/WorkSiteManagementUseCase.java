package com.condor.nexussoft.timeclock.organization.domain.port.in;

import com.condor.nexussoft.timeclock.organization.domain.WorkSite;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

/** Administración de centros de trabajo (RF-07), siempre acotada al tenant. */
public interface WorkSiteManagementUseCase {

    WorkSite create(UUID tenantId, WorkSiteCommands.CreateWorkSiteCommand command);

    WorkSite update(UUID tenantId, UUID id, WorkSiteCommands.UpdateWorkSiteCommand command);

    WorkSite changeStatus(UUID tenantId, UUID id, WorkSite.Status status);

    WorkSite get(UUID tenantId, UUID id);

    Paged<WorkSite> list(UUID tenantId, int page, int size, String search);
}
