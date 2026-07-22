package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshTokenJpaEntity r set r.revokedAt = :now "
            + "where r.familyId = :familyId and r.revokedAt is null")
    void revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);
}
