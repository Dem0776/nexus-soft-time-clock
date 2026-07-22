package com.condor.nexussoft.timeclock.scheduling.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftJpaRepository extends JpaRepository<ShiftJpaEntity, UUID> {

    Optional<ShiftJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ShiftJpaEntity> findByScheduleIdAndTenantId(UUID scheduleId, UUID tenantId);
}
