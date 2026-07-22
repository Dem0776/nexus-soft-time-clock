package com.condor.nexussoft.timeclock.identity.domain.port.out;

import com.condor.nexussoft.timeclock.identity.domain.port.in.UserView;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Puerto de salida para operaciones administrativas de usuarios (CRUD + roles). */
public interface UserAdminRepositoryPort {

    boolean existsByTenantAndEmail(UUID tenantId, String email);

    UserView create(UUID tenantId, String email, String passwordHash, String firstName,
                    String lastName, String employeeCode, Set<String> roleCodes);

    Optional<UserView> findByIdAndTenant(UUID id, UUID tenantId);

    Paged<UserView> findAllByTenant(UUID tenantId, int page, int size, String search);

    Optional<UserView> updateStatus(UUID id, UUID tenantId, String status);

    Optional<UserView> assignRoles(UUID id, UUID tenantId, Set<String> roleCodes);
}
