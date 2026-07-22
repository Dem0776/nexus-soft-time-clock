package com.condor.nexussoft.timeclock.scheduling.domain;

import java.util.UUID;

/** Horario (RF-08): contenedor de turnos dentro de un tenant. */
public class Schedule {

    public enum Status { ACTIVE, INACTIVE }

    private final UUID id;
    private final UUID tenantId;
    private final String code;
    private String name;
    private String timezone;
    private Status status;

    public Schedule(UUID id, UUID tenantId, String code, String name, String timezone, Status status) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.timezone = timezone;
        this.status = status;
    }

    public static Schedule create(UUID tenantId, String code, String name, String timezone) {
        return new Schedule(UUID.randomUUID(), tenantId, code, name, timezone, Status.ACTIVE);
    }

    public void update(String name, String timezone, Status status) {
        this.name = name;
        this.timezone = timezone;
        if (status != null) this.status = status;
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String code() { return code; }
    public String name() { return name; }
    public String timezone() { return timezone; }
    public Status status() { return status; }
}
