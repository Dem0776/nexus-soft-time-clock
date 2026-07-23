package com.condor.nexussoft.timeclock.shared.domain;

/**
 * Violación de autorización: la operación es válida pero el operador no tiene potestad
 * para ejecutarla (p. ej. otorgar un rol por encima de su nivel). Se traduce a HTTP 403
 * en la capa web. Es independiente del framework (mantiene la pureza hexagonal).
 */
public class AuthorizationException extends RuntimeException {

    private final String code;

    public AuthorizationException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Código estable de negocio, p. ej. "FORBIDDEN_ROLE_GRANT", útil para clientes e i18n. */
    public String getCode() {
        return code;
    }
}
