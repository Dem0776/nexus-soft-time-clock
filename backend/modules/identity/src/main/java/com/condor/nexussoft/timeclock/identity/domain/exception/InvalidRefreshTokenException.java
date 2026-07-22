package com.condor.nexussoft.timeclock.identity.domain.exception;

import com.condor.nexussoft.timeclock.shared.domain.DomainException;

/** Refresh token inválido, expirado o reutilizado (RN-41). */
public class InvalidRefreshTokenException extends DomainException {
    public InvalidRefreshTokenException() {
        super("INVALID_REFRESH_TOKEN", "Sesión inválida o expirada; inicie sesión nuevamente");
    }
}
