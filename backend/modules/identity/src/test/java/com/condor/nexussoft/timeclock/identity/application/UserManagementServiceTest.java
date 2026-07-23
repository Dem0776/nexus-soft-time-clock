package com.condor.nexussoft.timeclock.identity.application;

import com.condor.nexussoft.timeclock.identity.domain.port.in.GranterAuthority;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserCommands;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserView;
import com.condor.nexussoft.timeclock.identity.domain.port.out.PasswordHasherPort;
import com.condor.nexussoft.timeclock.identity.domain.port.out.UserAdminRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.AuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock UserAdminRepositoryPort users;
    @Mock PasswordHasherPort passwordHasher;

    UserManagementService service;
    final UUID tenantId = UUID.randomUUID();

    final GranterAuthority companyAdmin = new GranterAuthority(false, Set.of("COMPANY_ADMIN"));
    final GranterAuthority platformAdmin = new GranterAuthority(true, Set.of("SUPER_ADMIN"));

    @BeforeEach
    void setUp() {
        service = new UserManagementService(users, passwordHasher);
    }

    private UserCommands.CreateUserCommand command(Set<String> roleCodes) {
        return new UserCommands.CreateUserCommand("nuevo@demo.com", "Nuevo", "Usuario",
                "EMP-1", "secret123", roleCodes);
    }

    private UserView anyUserView() {
        return new UserView(UUID.randomUUID(), "nuevo@demo.com", "Nuevo", "Usuario",
                "EMP-1", "ACTIVE", List.of());
    }

    // --- create --------------------------------------------------------

    @Test
    void create_companyAdminOtorgandoSuperAdmin_rechaza() {
        assertThatThrownBy(() -> service.create(tenantId, companyAdmin, command(Set.of("SUPER_ADMIN"))))
                .isInstanceOf(AuthorizationException.class)
                .extracting(e -> ((AuthorizationException) e).getCode())
                .isEqualTo("FORBIDDEN_ROLE_GRANT");
        verify(users, never()).create(any(), any(), any(), any(), any(), any(), anySet());
    }

    @Test
    void create_companyAdminOtorgandoCompanyAdmin_rechaza() {
        assertThatThrownBy(() -> service.create(tenantId, companyAdmin, command(Set.of("COMPANY_ADMIN"))))
                .isInstanceOf(AuthorizationException.class);
        verifyNoInteractions(passwordHasher);
        verify(users, never()).create(any(), any(), any(), any(), any(), any(), anySet());
    }

    @Test
    void create_companyAdminOtorgandoRolesInferiores_ok() {
        when(users.existsByTenantAndEmail(tenantId, "nuevo@demo.com")).thenReturn(false);
        when(passwordHasher.hash("secret123")).thenReturn("hashed");
        when(users.create(eq(tenantId), any(), any(), any(), any(), any(), eq(Set.of("EMPLOYEE", "SUPERVISOR"))))
                .thenReturn(anyUserView());

        UserView result = service.create(tenantId, companyAdmin, command(Set.of("EMPLOYEE", "SUPERVISOR")));

        assertThat(result).isNotNull();
        verify(users).create(eq(tenantId), any(), eq("hashed"), any(), any(), any(), anySet());
    }

    @Test
    void create_platformAdminOtorgandoSuperAdmin_ok() {
        when(users.existsByTenantAndEmail(tenantId, "nuevo@demo.com")).thenReturn(false);
        when(passwordHasher.hash("secret123")).thenReturn("hashed");
        when(users.create(any(), any(), any(), any(), any(), any(), anySet())).thenReturn(anyUserView());

        UserView result = service.create(tenantId, platformAdmin, command(Set.of("SUPER_ADMIN")));

        assertThat(result).isNotNull();
    }

    @Test
    void create_companyAdminOtorgandoRolDesconocido_rechaza() {
        assertThatThrownBy(() -> service.create(tenantId, companyAdmin, command(Set.of("ROOT"))))
                .isInstanceOf(AuthorizationException.class);
        verify(users, never()).create(any(), any(), any(), any(), any(), any(), anySet());
    }

    // --- assignRoles ---------------------------------------------------

    @Test
    void assignRoles_companyAdminOtorgandoSuperAdmin_rechaza() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> service.assignRoles(tenantId, companyAdmin, userId, Set.of("SUPER_ADMIN")))
                .isInstanceOf(AuthorizationException.class)
                .extracting(e -> ((AuthorizationException) e).getCode())
                .isEqualTo("FORBIDDEN_ROLE_GRANT");
        verify(users, never()).assignRoles(any(), any(), anySet());
    }

    @Test
    void assignRoles_companyAdminOtorgandoRolInferior_ok() {
        UUID userId = UUID.randomUUID();
        when(users.assignRoles(userId, tenantId, Set.of("HR_ADMIN"))).thenReturn(Optional.of(anyUserView()));

        UserView result = service.assignRoles(tenantId, companyAdmin, userId, Set.of("HR_ADMIN"));

        assertThat(result).isNotNull();
        verify(users).assignRoles(userId, tenantId, Set.of("HR_ADMIN"));
    }
}
