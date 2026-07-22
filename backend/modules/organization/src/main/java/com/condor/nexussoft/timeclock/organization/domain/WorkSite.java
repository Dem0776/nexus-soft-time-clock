package com.condor.nexussoft.timeclock.organization.domain;

import java.util.UUID;

/** Centro de trabajo geolocalizado (RF-07). La geocerca asociada se gestiona en BC-05. */
public class WorkSite {

    public enum Status { ACTIVE, INACTIVE }

    private final UUID id;
    private final UUID tenantId;
    private final String code;
    private String name;
    private String address;
    private GeoPoint location;
    private String timezone;
    private Integer gpsAccuracyMaxM;
    private Boolean requirePhoto;
    private Boolean requireBiometric;
    private Status status;

    public WorkSite(UUID id, UUID tenantId, String code, String name, String address, GeoPoint location,
                    String timezone, Integer gpsAccuracyMaxM, Boolean requirePhoto, Boolean requireBiometric,
                    Status status) {
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

    public static WorkSite create(UUID tenantId, String code, String name, String address, GeoPoint location,
                                  String timezone, Integer gpsAccuracyMaxM, Boolean requirePhoto,
                                  Boolean requireBiometric) {
        return new WorkSite(UUID.randomUUID(), tenantId, code, name, address, location, timezone,
                gpsAccuracyMaxM, requirePhoto, requireBiometric, Status.ACTIVE);
    }

    public void update(String name, String address, GeoPoint location, String timezone,
                       Integer gpsAccuracyMaxM, Boolean requirePhoto, Boolean requireBiometric) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.timezone = timezone;
        this.gpsAccuracyMaxM = gpsAccuracyMaxM;
        this.requirePhoto = requirePhoto;
        this.requireBiometric = requireBiometric;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String code() { return code; }
    public String name() { return name; }
    public String address() { return address; }
    public GeoPoint location() { return location; }
    public String timezone() { return timezone; }
    public Integer gpsAccuracyMaxM() { return gpsAccuracyMaxM; }
    public Boolean requirePhoto() { return requirePhoto; }
    public Boolean requireBiometric() { return requireBiometric; }
    public Status status() { return status; }
}
