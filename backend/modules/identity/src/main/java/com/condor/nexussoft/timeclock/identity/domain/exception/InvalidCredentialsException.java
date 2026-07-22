package com.condor.nexussoft.timeclock.identity.domain.exception;

import com.condor.nexussoft.timeclock.shared.domain.DomainException;

/** Credenciales inválidas. Mensaje genérico para no revelar si el usuario existe. */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Usuario o contraseña incorrectos");
    }
}
