package com.condor.nexussoft.timeclock.geofencing.domain.port.out;

import com.condor.nexussoft.timeclock.geofencing.domain.SiteQrToken;

import java.util.UUID;

public interface SiteQrTokenRepositoryPort {

    /** Desactiva el QR activo previo del centro (rotación, ADR-006). */
    void deactivateActiveForSite(UUID workSiteId, UUID tenantId);

    SiteQrToken save(SiteQrToken token);
}
