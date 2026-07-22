package com.condor.nexussoft.timeclock.audit.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {

    Page<AuditLogJpaEntity> findByTenantId(UUID tenantId, Pageable pageable);
}
