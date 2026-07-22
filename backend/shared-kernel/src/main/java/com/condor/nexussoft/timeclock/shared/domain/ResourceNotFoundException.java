package com.condor.nexussoft.timeclock.shared.domain;

/** Recurso inexistente (se traduce a HTTP 404 en la capa web). */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Object id) {
        super("NOT_FOUND", resource + " no encontrado: " + id);
    }
}
