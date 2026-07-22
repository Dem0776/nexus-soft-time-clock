package com.condor.nexussoft.timeclock.geofencing.domain.port.out;

import com.condor.nexussoft.timeclock.geofencing.domain.Geofence;

import java.util.Optional;
import java.util.UUID;

public interface GeofenceRepositoryPort {

    Optional<Geofence> findActiveByWorkSite(UUID workSiteId, UUID tenantId);

    Geofence save(Geofence geofence);

    Geofence update(Geofence geofence);
}
