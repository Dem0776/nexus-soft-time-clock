package com.condor.nexussoft.timeclock.identity.domain.port.out;

import com.condor.nexussoft.timeclock.identity.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/** Puerto de salida hacia el almacén de refresh tokens (hash + familias). */
public interface RefreshTokenStorePort {

    void save(RefreshToken token);

    Optional<RefreshToken> findByHash(String tokenHash);

    /** Revoca toda la familia (detección de reutilización, RN-41). */
    void revokeFamily(UUID familyId);

    void update(RefreshToken token);
}
