package com.condor.nexussoft.timeclock.organization.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class ProjectJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(name = "starts_on")
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    protected ProjectJpaEntity() {
    }

    public ProjectJpaEntity(UUID id, UUID tenantId, String code, String name, String status,
                            LocalDate startsOn, LocalDate endsOn) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.status = status;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public LocalDate getStartsOn() { return startsOn; }
    public LocalDate getEndsOn() { return endsOn; }

    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setStartsOn(LocalDate startsOn) { this.startsOn = startsOn; }
    public void setEndsOn(LocalDate endsOn) { this.endsOn = endsOn; }
}
