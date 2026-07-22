package com.condor.nexussoft.timeclock.geofencing.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "geofences")
public class GeofenceJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "work_site_id", nullable = false)
    private UUID workSiteId;

    @Column(nullable = false)
    private String type;

    private Point center;

    @Column(name = "radius_m")
    private Double radiusM;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected GeofenceJpaEntity() {
    }

    public GeofenceJpaEntity(UUID id, UUID tenantId, UUID workSiteId, String type, Point center,
                             Double radiusM, boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.workSiteId = workSiteId;
        this.type = type;
        this.center = center;
        this.radiusM = radiusM;
        this.active = active;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getWorkSiteId() { return workSiteId; }
    public String getType() { return type; }
    public Point getCenter() { return center; }
    public Double getRadiusM() { return radiusM; }
    public boolean isActive() { return active; }

    public void setCenter(Point center) { this.center = center; }
    public void setRadiusM(Double radiusM) { this.radiusM = radiusM; }
}
