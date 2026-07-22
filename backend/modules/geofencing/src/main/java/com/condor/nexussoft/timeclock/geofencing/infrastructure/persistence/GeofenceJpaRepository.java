package com.condor.nexussoft.timeclock.geofencing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GeofenceJpaRepository extends JpaRepository<GeofenceJpaEntity, UUID> {

    Optional<GeofenceJpaEntity> findByWorkSiteIdAndTenantIdAndActiveTrue(UUID workSiteId, UUID tenantId);
}
