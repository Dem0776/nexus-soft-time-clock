package com.condor.nexussoft.timeclock.organization.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, UUID> {

    boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

    Optional<ProjectJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<ProjectJpaEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<ProjectJpaEntity> findByTenantIdAndNameContainingIgnoreCase(UUID tenantId, String name, Pageable pageable);
}
