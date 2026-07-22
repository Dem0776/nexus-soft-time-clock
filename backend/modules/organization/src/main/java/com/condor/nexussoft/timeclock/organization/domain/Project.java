package com.condor.nexussoft.timeclock.organization.domain;

import java.time.LocalDate;
import java.util.UUID;

/** Proyecto (RF-23). Agrupación lógica de trabajo dentro de un tenant. */
public class Project {

    public enum Status { ACTIVE, INACTIVE, CLOSED }

    private final UUID id;
    private final UUID tenantId;
    private final String code;
    private String name;
    private Status status;
    private LocalDate startsOn;
    private LocalDate endsOn;

    public Project(UUID id, UUID tenantId, String code, String name, Status status,
                   LocalDate startsOn, LocalDate endsOn) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.status = status;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
    }

    public static Project create(UUID tenantId, String code, String name, LocalDate startsOn, LocalDate endsOn) {
        return new Project(UUID.randomUUID(), tenantId, code, name, Status.ACTIVE, startsOn, endsOn);
    }

    public void update(String name, Status status, LocalDate startsOn, LocalDate endsOn) {
        this.name = name;
        if (status != null) this.status = status;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String code() { return code; }
    public String name() { return name; }
    public Status status() { return status; }
    public LocalDate startsOn() { return startsOn; }
    public LocalDate endsOn() { return endsOn; }
}
