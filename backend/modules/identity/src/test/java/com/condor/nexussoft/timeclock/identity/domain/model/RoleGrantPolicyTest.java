package com.condor.nexussoft.timeclock.identity.domain.model;

import com.condor.nexussoft.timeclock.shared.domain.AuthorizationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class RoleGrantPolicyTest {

    private static final Set<String> COMPANY_ADMIN = Set.of("COMPANY_ADMIN");

    // --- canGrant (filtrado del catálogo, RF-22) -----------------------

    @Test
    void canGrant_companyAdmin_soloRolesInferiores() {
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "HR_ADMIN")).isTrue();
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "SUPERVISOR")).isTrue();
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "AUDITOR")).isTrue();
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "EMPLOYEE")).isTrue();
        // el propio nivel y superiores no son otorgables
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "COMPANY_ADMIN")).isFalse();
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "SUPER_ADMIN")).isFalse();
        // rol desconocido: fail-safe
        assertThat(RoleGrantPolicy.canGrant(false, COMPANY_ADMIN, "ROOT")).isFalse();
    }

    @Test
    void canGrant_platformAdmin_cualquierRol() {
        assertThat(RoleGrantPolicy.canGrant(true, Set.of("SUPER_ADMIN"), "SUPER_ADMIN")).isTrue();
        assertThat(RoleGrantPolicy.canGrant(true, Set.of(), "ROOT")).isTrue();
    }

    @Test
    void canGrant_sinRoles_noOtorgaNada() {
        assertThat(RoleGrantPolicy.canGrant(false, Set.of(), "EMPLOYEE")).isFalse();
    }

    // --- assertCanGrant ------------------------------------------------

    @Test
    void assertCanGrant_rolSuperior_lanza() {
        assertThatThrownBy(() -> RoleGrantPolicy.assertCanGrant(false, COMPANY_ADMIN, Set.of("SUPER_ADMIN")))
                .isInstanceOf(AuthorizationException.class)
                .extracting(e -> ((AuthorizationException) e).getCode())
                .isEqualTo("FORBIDDEN_ROLE_GRANT");
    }

    @Test
    void assertCanGrant_rolesInferioresYVacio_ok() {
        assertThatCode(() -> RoleGrantPolicy.assertCanGrant(false, COMPANY_ADMIN, Set.of("EMPLOYEE", "SUPERVISOR")))
                .doesNotThrowAnyException();
        assertThatCode(() -> RoleGrantPolicy.assertCanGrant(false, COMPANY_ADMIN, Set.of()))
                .doesNotThrowAnyException();
    }
}
