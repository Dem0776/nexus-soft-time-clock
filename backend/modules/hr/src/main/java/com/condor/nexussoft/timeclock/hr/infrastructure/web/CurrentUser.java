package com.condor.nexussoft.timeclock.hr.infrastructure.web;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Extrae tenant y usuario del token JWT autenticado.
 *
 * NOTA DE INTEGRACIÓN: los nombres de claim deben coincidir con los que emite
 * identity/JwtAccessTokenIssuer. Si tu plataforma expone un {@code TenantContext},
 * puedes sustituir {@link #tenantId(Authentication)} por {@code TenantContext.getTenantId()}.
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static UUID tenantId(Authentication auth) {
        Object t = claim(auth, "tid");
        if (t == null) t = claim(auth, "tenant_id");
        if (t == null) t = claim(auth, "tenantId");
        return t == null ? null : UUID.fromString(t.toString());
    }

    public static UUID userId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object uid = jwt.getToken().getClaims().get("uid");
            if (uid == null) uid = jwt.getToken().getSubject();
            return UUID.fromString(uid.toString());
        }
        return UUID.fromString(auth.getName());
    }

    private static Object claim(Authentication auth, String name) {
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getClaims().get(name);
        }
        return null;
    }
}
