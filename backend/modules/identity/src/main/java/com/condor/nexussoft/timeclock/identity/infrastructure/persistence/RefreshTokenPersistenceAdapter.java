package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import com.condor.nexussoft.timeclock.identity.domain.model.RefreshToken;
import com.condor.nexussoft.timeclock.identity.domain.port.out.RefreshTokenStorePort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenPersistenceAdapter implements RefreshTokenStorePort {

    private final RefreshTokenJpaRepository jpa;

    public RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(RefreshToken token) {
        jpa.save(toEntity(token));
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void revokeFamily(UUID familyId) {
        jpa.revokeFamily(familyId, Instant.now());
    }

    @Override
    public void update(RefreshToken token) {
        jpa.save(toEntity(token));
    }

    private RefreshTokenJpaEntity toEntity(RefreshToken t) {
        return new RefreshTokenJpaEntity(
                t.id(), t.tenantId(), t.userId(), t.familyId(),
                t.tokenHash(), t.replacedById(), t.expiresAt(), t.revokedAt());
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity e) {
        return new RefreshToken(
                e.getId(), e.getFamilyId(), e.getUserId(), e.getTenantId(),
                e.getTokenHash(), e.getExpiresAt(), e.getRevokedAt(), e.getReplacedBy());
    }
}
