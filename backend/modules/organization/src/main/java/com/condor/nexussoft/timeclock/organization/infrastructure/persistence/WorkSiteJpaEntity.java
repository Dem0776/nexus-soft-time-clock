package com.condor.nexussoft.timeclock.organization.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "work_sites")
public class WorkSiteJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private Point location;      // geography(Point,4326) — hibernate-spatial

    private String timezone;

    @Column(name = "gps_accuracy_max_m")
    private Integer gpsAccuracyMaxM;

    @Column(name = "require_photo")
    private Boolean requirePhoto;

    @Column(name = "require_biometric")
    private Boolean requireBiometric;

    @Column(nullable = false)
    private String status;

    protected WorkSiteJpaEntity() {
    }

    public WorkSiteJpaEntity(UUID id, UUID tenantId, String code, String name, String address, Point location,
                             String timezone, Integer gpsAccuracyMaxM, Boolean requirePhoto,
                             Boolean requireBiometric, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.address = address;
        this.location = location;
        this.timezone = timezone;
        this.gpsAccuracyMaxM = gpsAccuracyMaxM;
        this.requirePhoto = requirePhoto;
        this.requireBiometric = requireBiometric;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Point getLocation() { return location; }
    public String getTimezone() { return timezone; }
    public Integer getGpsAccuracyMaxM() { return gpsAccuracyMaxM; }
    public Boolean getRequirePhoto() { return requirePhoto; }
    public Boolean getRequireBiometric() { return requireBiometric; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLocation(Point location) { this.location = location; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setGpsAccuracyMaxM(Integer v) { this.gpsAccuracyMaxM = v; }
    public void setRequirePhoto(Boolean v) { this.requirePhoto = v; }
    public void setRequireBiometric(Boolean v) { this.requireBiometric = v; }
    public void setStatus(String status) { this.status = status; }
}
