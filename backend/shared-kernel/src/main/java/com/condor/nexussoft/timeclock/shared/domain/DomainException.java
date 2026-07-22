package com.condor.nexussoft.timeclock.shared.domain;

/**
 * Excepción base para violaciones de reglas de negocio (RN). Se traduce a una
 * respuesta HTTP uniforme (ProblemDetail) en la capa web (RNF: manejo uniforme de errores).
 */
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Código estable de negocio, p.ej. "OUT_OF_GEOFENCE", útil para clientes e i18n. */
    public String getCode() {
        return code;
    }
}
