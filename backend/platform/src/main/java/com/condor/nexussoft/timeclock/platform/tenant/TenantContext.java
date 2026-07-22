package com.condor.nexussoft.timeclock.platform.tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Portador del tenant actual por hilo de petición. El tenant se deriva SIEMPRE del
 * token de seguridad (RN-31), nunca de parámetros del cliente. Un filtro/interceptor
 * lo establece al inicio de la petición y lo limpia al final.
 *
 * En la Iteración 5 (seguridad) se conecta con el JWT; aquí queda la base transversal.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID tenantId) {
        CURRENT.set(tenantId);
    }

    public static Optional<UUID> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static UUID require() {
        UUID tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No hay tenant en el contexto de la petición");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
