package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.util.UUID;

/** Verifica que una posición esté dentro de la geocerca del centro (delegado a Geofencing). */
public interface GeofenceCheckPort {

    record GeofenceCheck(boolean exists, boolean withinRadius, double distanceM, double accuracyMaxM) {
        public static GeofenceCheck notFound(double accuracyMaxM) {
            return new GeofenceCheck(false, false, Double.NaN, accuracyMaxM);
        }
    }

    GeofenceCheck check(UUID tenantId, UUID workSiteId, double latitude, double longitude);
}
