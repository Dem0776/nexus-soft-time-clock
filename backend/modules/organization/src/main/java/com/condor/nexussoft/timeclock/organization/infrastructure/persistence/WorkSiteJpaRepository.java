package com.condor.nexussoft.timeclock.organization.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkSiteJpaRepository extends JpaRepository<WorkSiteJpaEntity, UUID> {

    boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

    Optional<WorkSiteJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<WorkSiteJpaEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<WorkSiteJpaEntity> findByTenantIdAndNameContainingIgnoreCase(UUID tenantId, String name, Pageable pageable);
}
