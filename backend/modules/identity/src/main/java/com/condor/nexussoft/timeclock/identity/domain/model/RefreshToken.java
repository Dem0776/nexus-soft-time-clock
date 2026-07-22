package com.condor.nexussoft.timeclock.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token persistido (hash, nunca el valor en claro). Pertenece a una
 * "familia" de rotación: reutilizar uno consumido revoca toda la familia (RN-41, ADR-007).
 */
public class RefreshToken {

    private final UUID id;
    private final UUID familyId;
    private final UUID userId;
    private final UUID tenantId;
    private final String tokenHash;
    private final Instant expiresAt;
    private Instant revokedAt;
    private UUID replacedById;

    public RefreshToken(UUID id, UUID familyId, UUID userId, UUID tenantId, String tokenHash,
                        Instant expiresAt, Instant revokedAt, UUID replacedById) {
        this.id = id;
        this.familyId = familyId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.replacedById = replacedById;
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public boolean isReused() {
        return revokedAt != null || replacedById != null;
    }

    public void revoke(Instant now) {
        if (this.revokedAt == null) {
            this.revokedAt = now;
        }
    }

    public void markReplacedBy(UUID newTokenId, Instant now) {
        this.replacedById = newTokenId;
        revoke(now);
    }

    public UUID id()          { return id; }
    public UUID familyId()    { return familyId; }
    public UUID userId()      { return userId; }
    public UUID tenantId()    { return tenantId; }
    public String tokenHash() { return tokenHash; }
    public Instant expiresAt() { return expiresAt; }
    public Instant revokedAt() { return revokedAt; }
    public UUID replacedById() { return replacedById; }
}
