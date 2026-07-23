package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.port.in.GranterAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;

/** Deriva la identidad del operador (potestad de delegación de roles) desde el access token. */
final class GranterAuthorities {

    private GranterAuthorities() {
    }

    static GranterAuthority from(Jwt jwt) {
        boolean platformAdmin = Boolean.TRUE.equals(jwt.getClaimAsBoolean("platform_admin"));
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new GranterAuthority(platformAdmin, roles == null ? Set.of() : Set.copyOf(roles));
    }
}
