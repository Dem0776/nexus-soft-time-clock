package com.condor.nexussoft.timeclock.notifications.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String type;

    private String title;

    private String body;

    @Column(nullable = false)
    private String status;

    protected NotificationJpaEntity() {
    }

    public NotificationJpaEntity(UUID id, UUID tenantId, UUID userId, String channel, String type,
                                 String title, String body, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.channel = channel;
        this.type = type;
        this.title = title;
        this.body = body;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getChannel() { return channel; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getStatus() { return status; }
}
