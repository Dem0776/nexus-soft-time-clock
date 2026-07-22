package com.condor.nexussoft.timeclock.scheduling.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "schedules")
public class ScheduleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String timezone;

    @Column(nullable = false)
    private String status;

    protected ScheduleJpaEntity() {
    }

    public ScheduleJpaEntity(UUID id, UUID tenantId, String code, String name, String timezone, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.timezone = timezone;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getTimezone() { return timezone; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setStatus(String status) { this.status = status; }
}
