package com.condor.nexussoft.timeclock.identity.domain.model;

import java.util.Set;
import java.util.UUID;

/** Rol RBAC con su conjunto de permisos. */
public record Role(UUID id, String code, Set<Permission> permissions) {
    public Role {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
