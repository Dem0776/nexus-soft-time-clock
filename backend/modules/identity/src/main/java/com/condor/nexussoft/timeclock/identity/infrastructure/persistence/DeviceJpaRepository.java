package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, UUID> {

    Optional<DeviceJpaEntity> findByTenantIdAndUserIdAndDeviceIdentifier(
            UUID tenantId, UUID userId, String deviceIdentifier);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    List<DeviceJpaEntity> findByTenantIdAndUserIdOrderByCreatedAtDesc(UUID tenantId, UUID userId);

    Optional<DeviceJpaEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}
