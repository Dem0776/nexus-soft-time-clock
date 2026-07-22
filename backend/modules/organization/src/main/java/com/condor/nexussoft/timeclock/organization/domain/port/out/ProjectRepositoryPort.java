package com.condor.nexussoft.timeclock.organization.domain.port.out;

import com.condor.nexussoft.timeclock.organization.domain.Project;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepositoryPort {

    Project save(Project project);

    Project update(Project project);

    Optional<Project> findByIdAndTenant(UUID id, UUID tenantId);

    boolean existsByTenantAndCode(UUID tenantId, String code);

    Paged<Project> findAllByTenant(UUID tenantId, int page, int size, String search);
}
