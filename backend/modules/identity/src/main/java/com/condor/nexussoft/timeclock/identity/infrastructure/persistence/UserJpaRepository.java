package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByTenantIdAndEmail(UUID tenantId, String email);

    Optional<UserJpaEntity> findByPlatformAdminTrueAndEmail(String email);

    // --- Administración de usuarios (BC-01) ---
    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    Optional<UserJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<UserJpaEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<UserJpaEntity> findByTenantIdAndEmailContainingIgnoreCase(UUID tenantId, String email, Pageable pageable);
}
