package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {

    /** Roles plantilla del sistema (tenant_id IS NULL). */
    List<RoleJpaEntity> findByTenantIdIsNull();

    List<RoleJpaEntity> findByCodeInAndTenantIdIsNull(Collection<String> codes);
}
