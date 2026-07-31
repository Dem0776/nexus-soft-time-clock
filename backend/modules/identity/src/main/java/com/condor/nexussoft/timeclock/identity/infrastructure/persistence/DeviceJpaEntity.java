package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapea la tabla {@code devices} (creada en V2). {@code created_at}/{@code updated_at} los
 * gestiona la BD (default + trigger); {@code push_token} no se mapea (opcional, gestionado aparte).
 */
@Entity
@Table(name = "devices")
public class DeviceJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_identifier", nullable = false)
    private String deviceIdentifier;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "model")
    private String model;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "is_trusted", nullable = false)
    private boolean trusted;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected DeviceJpaEntity() {
    }

    public DeviceJpaEntity(UUID id, UUID tenantId, UUID userId, String deviceIdentifier, String platform,
                           String model, String osVersion, boolean trusted, String status, Instant lastSeenAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.deviceIdentifier = deviceIdentifier;
        this.platform = platform;
        this.model = model;
        this.osVersion = osVersion;
        this.trusted = trusted;
        this.status = status;
        this.lastSeenAt = lastSeenAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public String getPlatform() { return platform; }
    public String getModel() { return model; }
    public String getOsVersion() { return osVersion; }
    public boolean isTrusted() { return trusted; }
    public String getStatus() { return status; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public Instant getCreatedAt() { return createdAt; }
}
