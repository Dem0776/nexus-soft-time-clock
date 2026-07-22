package com.condor.nexussoft.timeclock.organization.domain.port.out;

import com.condor.nexussoft.timeclock.organization.domain.WorkSite;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.Optional;
import java.util.UUID;

public interface WorkSiteRepositoryPort {

    WorkSite save(WorkSite workSite);

    WorkSite update(WorkSite workSite);

    Optional<WorkSite> findByIdAndTenant(UUID id, UUID tenantId);

    boolean existsByTenantAndCode(UUID tenantId, String code);

    Paged<WorkSite> findAllByTenant(UUID tenantId, int page, int size, String search);
}
