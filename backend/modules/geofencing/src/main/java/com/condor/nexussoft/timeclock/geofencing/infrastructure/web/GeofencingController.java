package com.condor.nexussoft.timeclock.geofencing.infrastructure.web;

import com.condor.nexussoft.timeclock.geofencing.domain.port.in.GeofencingUseCase;
import com.condor.nexussoft.timeclock.geofencing.infrastructure.web.dto.GeofencingDtos.*;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Geocercas y generación/rotación del QR firmado por centro (RF-10, RF-14). Requiere {@code geofence:manage}. */
@RestController
@RequestMapping("/api/v1/work-sites/{workSiteId}")
@PreAuthorize("hasAuthority('geofence:manage')")
public class GeofencingController {

    private final GeofencingUseCase geofencing;

    public GeofencingController(GeofencingUseCase geofencing) {
        this.geofencing = geofencing;
    }

    @PutMapping("/geofence")
    public GeofenceResponse upsertGeofence(@PathVariable UUID workSiteId, @Valid @RequestBody GeofenceRequest r) {
        return GeofenceResponse.from(
                geofencing.upsertGeofence(tenant(), workSiteId, r.latitude(), r.longitude(), r.radiusM()));
    }

    @GetMapping("/geofence")
    public GeofenceResponse getGeofence(@PathVariable UUID workSiteId) {
        return GeofenceResponse.from(geofencing.getGeofence(tenant(), workSiteId));
    }

    @PostMapping("/qr")
    @ResponseStatus(HttpStatus.CREATED)
    public QrResponse generateQr(@PathVariable UUID workSiteId) {
        return QrResponse.from(geofencing.generateQr(tenant(), workSiteId));
    }

    private UUID tenant() {
        return TenantContext.require();
    }
}
