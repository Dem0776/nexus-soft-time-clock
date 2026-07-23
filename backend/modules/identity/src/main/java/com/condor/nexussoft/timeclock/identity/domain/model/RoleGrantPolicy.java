package com.condor.nexussoft.timeclock.identity.domain.model;

import com.condor.nexussoft.timeclock.shared.domain.AuthorizationException;

import java.util.Map;
import java.util.Set;

/**
 * Política de delegación de roles (jerarquía RBAC, doc §2.2). Un operador solo puede
 * otorgar roles <b>estrictamente inferiores</b> al suyo; el {@code SUPER_ADMIN} de
 * plataforma no tiene restricción. Impide la escalada de privilegios (p. ej. que un
 * {@code COMPANY_ADMIN} se auto-conceda {@code SUPER_ADMIN} y cruce tenants — RN-30..33).
 *
 * <p>La frontera real es el servidor: el frontend replica esta regla solo por UX.
 */
public final class RoleGrantPolicy {

    /** Nivel de privilegio por código de rol. Un rol desconocido no es otorgable (fail-safe). */
    private static final Map<String, Integer> RANK = Map.of(
            "SUPER_ADMIN", 100,
            "COMPANY_ADMIN", 80,
            "HR_ADMIN", 60,
            "SUPERVISOR", 60,
            "AUDITOR", 40,
            "EMPLOYEE", 20);

    private RoleGrantPolicy() {
    }

    /**
     * Verifica que el operador pueda otorgar todos los roles solicitados.
     *
     * @param platformAdmin  true si el operador es SUPER_ADMIN de plataforma (sin restricción)
     * @param callerRoles    códigos de rol del operador
     * @param requestedRoles códigos de rol a otorgar
     * @throws AuthorizationException {@code FORBIDDEN_ROLE_GRANT} si algún rol supera su nivel
     */
    public static void assertCanGrant(boolean platformAdmin, Set<String> callerRoles,
                                      Set<String> requestedRoles) {
        if (platformAdmin || requestedRoles == null || requestedRoles.isEmpty()) {
            return;
        }
        int callerMax = maxRank(callerRoles);
        for (String role : requestedRoles) {
            int rank = RANK.getOrDefault(role, Integer.MAX_VALUE);
            if (rank >= callerMax) {
                throw new AuthorizationException("FORBIDDEN_ROLE_GRANT",
                        "No tiene potestad para otorgar el rol " + role);
            }
        }
    }

    private static int maxRank(Set<String> roleCodes) {
        if (roleCodes == null) {
            return 0;
        }
        return roleCodes.stream().mapToInt(r -> RANK.getOrDefault(r, 0)).max().orElse(0);
    }
}
