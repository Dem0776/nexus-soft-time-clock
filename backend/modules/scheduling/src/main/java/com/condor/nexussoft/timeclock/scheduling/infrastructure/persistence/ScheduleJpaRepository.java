package com.condor.nexussoft.timeclock.scheduling.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleJpaRepository extends JpaRepository<ScheduleJpaEntity, UUID> {

    boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

    Optional<ScheduleJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<ScheduleJpaEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<ScheduleJpaEntity> findByTenantIdAndNameContainingIgnoreCase(UUID tenantId, String name, Pageable pageable);
}
