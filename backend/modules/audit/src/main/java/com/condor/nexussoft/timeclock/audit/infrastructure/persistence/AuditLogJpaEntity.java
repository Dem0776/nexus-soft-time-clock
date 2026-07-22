package com.condor.nexussoft.timeclock.audit.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values")
    private String newValues;

    protected AuditLogJpaEntity() {
    }

    public AuditLogJpaEntity(UUID id, UUID tenantId, Instant createdAt, UUID actorUserId, String action,
                             String resourceType, String resourceId, String newValues) {
        this.id = id;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
        this.actorUserId = actorUserId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.newValues = newValues;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getActorUserId() { return actorUserId; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getNewValues() { return newValues; }
}
