package com.condor.nexussoft.timeclock.scheduling.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftAssignmentJpaRepository extends JpaRepository<ShiftAssignmentJpaEntity, UUID> {

    List<ShiftAssignmentJpaEntity> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
