package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Configuración de un tipo de evento intermedio para una empresa (HU-12 CA1). */
@Entity
@Table(name = "attendance_event_type_settings")
public class EventTypeConfigJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private boolean enabled;

    @Column
    private String label;

    protected EventTypeConfigJpaEntity() {
    }

    public EventTypeConfigJpaEntity(UUID id, UUID tenantId, String eventType, boolean enabled, String label) {
        this.id = id;
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.enabled = enabled;
        this.label = label;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getEventType() { return eventType; }
    public boolean isEnabled() { return enabled; }
    public String getLabel() { return label; }
}
