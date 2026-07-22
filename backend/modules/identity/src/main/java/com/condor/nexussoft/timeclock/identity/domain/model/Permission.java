package com.condor.nexussoft.timeclock.identity.domain.model;

/** Permiso RBAC (recurso:acción), p.ej. "attendance:register". */
public record Permission(String code) {
    public Permission {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("permiso inválido");
        }
    }
}
