package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventTypeConfigJpaRepository extends JpaRepository<EventTypeConfigJpaEntity, UUID> {

    List<EventTypeConfigJpaEntity> findByTenantId(UUID tenantId);

    void deleteByTenantId(UUID tenantId);
}
