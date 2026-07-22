package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, UUID> {

    Optional<IdempotencyKeyJpaEntity> findByTenantIdAndOperationUuid(UUID tenantId, UUID operationUuid);
}
