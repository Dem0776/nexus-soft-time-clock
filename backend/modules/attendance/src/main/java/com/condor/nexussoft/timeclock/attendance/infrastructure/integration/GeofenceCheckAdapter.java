package com.condor.nexussoft.timeclock.attendance.infrastructure.integration;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.GeofenceCheckPort;
import com.condor.nexussoft.timeclock.geofencing.domain.Geofence;
import com.condor.nexussoft.timeclock.geofencing.domain.port.in.GeofencingUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Puente hacia Geofencing: obtiene la geocerca y evalúa distancia/pertenencia (RN-13, RN-14). */
@Component
public class GeofenceCheckAdapter implements GeofenceCheckPort {

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private final GeofencingUseCase geofencing;
    private final double defaultAccuracyMaxM;

    public GeofenceCheckAdapter(GeofencingUseCase geofencing,
                                @Value("${attendance.default-gps-accuracy-max-m:50}") double defaultAccuracyMaxM) {
        this.geofencing = geofencing;
        this.defaultAccuracyMaxM = defaultAccuracyMaxM;
    }

    @Override
    public GeofenceCheck check(UUID tenantId, UUID workSiteId, double latitude, double longitude) {
        // Búsqueda no excepcional: un centro sin geocerca es una condición de negocio normal
        // (deriva en OUT_OF_GEOFENCE). Lanzar aquí marcaría como rollback-only la transacción
        // del registro de asistencia, provocando UnexpectedRollbackException en el commit.
        return geofencing.findGeofence(tenantId, workSiteId)
                .map(g -> {
                    double distance = haversine(g.center().latitude(), g.center().longitude(), latitude, longitude);
                    boolean within = distance <= g.radiusM();
                    return new GeofenceCheck(true, within, distance, defaultAccuracyMaxM);
                })
                .orElseGet(() -> GeofenceCheck.notFound(defaultAccuracyMaxM));
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
