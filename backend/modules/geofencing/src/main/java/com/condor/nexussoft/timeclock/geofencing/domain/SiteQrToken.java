package com.condor.nexussoft.timeclock.geofencing.domain;

import java.time.Instant;
import java.util.UUID;

/** Token de QR de un centro: nonce + vigencia, firmado externamente (ADR-006, RN-25). */
public class SiteQrToken {

    private final UUID id;
    private final UUID tenantId;
    private final UUID workSiteId;
    private final String nonce;
    private final String keyId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private boolean active;

    public SiteQrToken(UUID id, UUID tenantId, UUID workSiteId, String nonce, String keyId,
                       Instant issuedAt, Instant expiresAt, boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.workSiteId = workSiteId;
        this.nonce = nonce;
        this.keyId = keyId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public static SiteQrToken issue(UUID tenantId, UUID workSiteId, String nonce, String keyId,
                                    Instant issuedAt, Instant expiresAt) {
        return new SiteQrToken(UUID.randomUUID(), tenantId, workSiteId, nonce, keyId, issuedAt, expiresAt, true);
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID workSiteId() { return workSiteId; }
    public String nonce() { return nonce; }
    public String keyId() { return keyId; }
    public Instant issuedAt() { return issuedAt; }
    public Instant expiresAt() { return expiresAt; }
    public boolean active() { return active; }
}
