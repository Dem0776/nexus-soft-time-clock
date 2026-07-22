package com.condor.nexussoft.timeclock.shared.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object del identificador de tenant (empresa). Refuerza el tipado fuerte
 * para evitar confundir ids en las firmas (Effective Java: tipos específicos).
 */
public record TenantId(UUID value) {

    public TenantId {
        Objects.requireNonNull(value, "tenantId no puede ser null");
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }

    public static TenantId of(String value) {
        return new TenantId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
