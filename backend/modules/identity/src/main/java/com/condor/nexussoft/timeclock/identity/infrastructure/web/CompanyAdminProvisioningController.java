package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.port.in.UserCommands;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserManagementUseCase;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserView;
import com.condor.nexussoft.timeclock.identity.infrastructure.web.dto.ProvisionCompanyAdminRequest;
import com.condor.nexussoft.timeclock.identity.infrastructure.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

/**
 * Aprovisionamiento del administrador inicial (COMPANY_ADMIN) de una empresa, ejecutado por el
 * SUPER_ADMIN de plataforma (RF-13). Es una operación de plataforma: el tenant se toma del path
 * ({@code companyId} == {@code tenant_id}, la empresa ES el tenant), no de {@link
 * com.condor.nexussoft.timeclock.platform.tenant.TenantContext} — por eso funciona para un
 * SUPER_ADMIN que no tiene tenant propio. Requiere {@code company:manage}.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/admin")
@PreAuthorize("hasAuthority('company:manage')")
public class CompanyAdminProvisioningController {

    private final UserManagementUseCase users;

    public CompanyAdminProvisioningController(UserManagementUseCase users) {
        this.users = users;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse provision(@PathVariable UUID companyId,
                                  @Valid @RequestBody ProvisionCompanyAdminRequest r,
                                  @AuthenticationPrincipal Jwt jwt) {
        UserView view = users.create(companyId, GranterAuthorities.from(jwt),
                new UserCommands.CreateUserCommand(
                        r.email(), r.firstName(), r.lastName(), r.employeeCode(),
                        r.password(), Set.of("COMPANY_ADMIN")));
        return UserResponse.from(view);
    }
}
