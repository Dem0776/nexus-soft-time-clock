package com.condor.nexussoft.timeclock.identity.infrastructure.security;

import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Establece el tenant de la petición a partir del claim {@code tenant_id} del JWT ya
 * autenticado (RN-31: el tenant nunca viene de parámetros del cliente). Limpia el
 * contexto al finalizar para no filtrar estado entre peticiones del pool de hilos.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String tenant = jwtAuth.getToken().getClaimAsString("tenant_id");
                if (tenant != null && !tenant.isBlank()) {
                    TenantContext.set(UUID.fromString(tenant));
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
