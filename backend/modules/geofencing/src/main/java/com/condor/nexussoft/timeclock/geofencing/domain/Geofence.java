package com.condor.nexussoft.timeclock.geofencing.domain;

import java.util.UUID;

/** Geocerca circular por centro de trabajo (RN-13). El soporte poligonal se añade después (S-02). */
public class Geofence {

    private final UUID id;
    private final UUID tenantId;
    private final UUID workSiteId;
    private GeoPoint center;
    private double radiusM;
    private boolean active;

    public Geofence(UUID id, UUID tenantId, UUID workSiteId, GeoPoint center, double radiusM, boolean active) {
        if (radiusM <= 0) {
            throw new IllegalArgumentException("el radio debe ser mayor que 0");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.workSiteId = workSiteId;
        this.center = center;
        this.radiusM = radiusM;
        this.active = active;
    }

    public static Geofence createCircle(UUID tenantId, UUID workSiteId, GeoPoint center, double radiusM) {
        return new Geofence(UUID.randomUUID(), tenantId, workSiteId, center, radiusM, true);
    }

    public void redefine(GeoPoint center, double radiusM) {
        if (radiusM <= 0) {
            throw new IllegalArgumentException("el radio debe ser mayor que 0");
        }
        this.center = center;
        this.radiusM = radiusM;
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID workSiteId() { return workSiteId; }
    public GeoPoint center() { return center; }
    public double radiusM() { return radiusM; }
    public boolean active() { return active; }
}
