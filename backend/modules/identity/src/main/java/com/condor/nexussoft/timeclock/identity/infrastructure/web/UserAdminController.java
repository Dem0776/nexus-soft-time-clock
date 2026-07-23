package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.port.in.GranterAuthority;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserCommands;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserManagementUseCase;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserView;
import com.condor.nexussoft.timeclock.identity.infrastructure.web.dto.*;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Administración de usuarios del tenant (RF-06, RF-22). Requiere {@code user:manage}. */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('user:manage')")
public class UserAdminController {

    private final UserManagementUseCase users;

    public UserAdminController(UserManagementUseCase users) {
        this.users = users;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest r, @AuthenticationPrincipal Jwt jwt) {
        UserView view = users.create(tenant(), granter(jwt), new UserCommands.CreateUserCommand(
                r.email(), r.firstName(), r.lastName(), r.employeeCode(), r.password(), r.roleCodes()));
        return UserResponse.from(view);
    }

    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Paged<UserView> result = users.list(tenant(), page, size, search);
        return PageResponse.of(result.items().stream().map(UserResponse::from).toList(),
                result.page(), result.size(), result.total());
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return UserResponse.from(users.get(tenant(), id));
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest r) {
        return UserResponse.from(users.updateStatus(tenant(), id, r.status()));
    }

    @PutMapping("/{id}/roles")
    public UserResponse assignRoles(@PathVariable UUID id, @Valid @RequestBody AssignRolesRequest r,
                                    @AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(users.assignRoles(tenant(), granter(jwt), id, r.roleCodes()));
    }

    private UUID tenant() {
        return TenantContext.require();
    }

    /** Deriva la identidad del operador (potestad de delegación de roles) desde el access token. */
    private GranterAuthority granter(Jwt jwt) {
        boolean platformAdmin = Boolean.TRUE.equals(jwt.getClaimAsBoolean("platform_admin"));
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new GranterAuthority(platformAdmin, roles == null ? Set.of() : Set.copyOf(roles));
    }
}
