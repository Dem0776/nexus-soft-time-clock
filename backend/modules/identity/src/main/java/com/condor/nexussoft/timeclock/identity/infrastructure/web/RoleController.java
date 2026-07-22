package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.infrastructure.persistence.RoleJpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Listado de roles disponibles (plantillas del sistema) para asignación (RF-22).
 * La personalización de roles por tenant se aborda en una iteración posterior.
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
    public List<RoleResponse> list() {
        return roles.findByTenantIdIsNull().stream()
                .map(r -> new RoleResponse(r.getCode(), r.getName()))
                .toList();
    }
}
