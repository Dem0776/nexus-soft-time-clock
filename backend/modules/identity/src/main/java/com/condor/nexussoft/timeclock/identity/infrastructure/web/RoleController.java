package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.model.RoleGrantPolicy;
import com.condor.nexussoft.timeclock.identity.domain.port.in.GranterAuthority;
import com.condor.nexussoft.timeclock.identity.infrastructure.persistence.RoleJpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Listado de roles disponibles (plantillas del sistema) para asignación (RF-22). El catálogo se
 * filtra por la potestad de delegación del solicitante (misma jerarquía que {@link RoleGrantPolicy}):
 * un operador solo ve los roles que puede otorgar, evitando exponer {@code SUPER_ADMIN} a un
 * {@code COMPANY_ADMIN}. La personalización de roles por tenant se aborda en una iteración posterior.
 */
@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasAuthority('role:manage')")
public class RoleController {

    private final RoleJpaRepository roles;

    public RoleController(RoleJpaRepository roles) {
        this.roles = roles;
    }

    public record RoleResponse(String code, String name) {
    }

    @GetMapping
    public List<RoleResponse> list(@AuthenticationPrincipal Jwt jwt) {
        GranterAuthority granter = GranterAuthorities.from(jwt);
        return roles.findByTenantIdIsNull().stream()
                .filter(r -> RoleGrantPolicy.canGrant(granter.platformAdmin(), granter.roleCodes(), r.getCode()))
                .map(r -> new RoleResponse(r.getCode(), r.getName()))
                .toList();
    }
}
