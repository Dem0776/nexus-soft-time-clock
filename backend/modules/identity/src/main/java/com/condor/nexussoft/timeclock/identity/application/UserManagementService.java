package com.condor.nexussoft.timeclock.identity.application;

import com.condor.nexussoft.timeclock.identity.domain.model.Email;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserCommands;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserManagementUseCase;
import com.condor.nexussoft.timeclock.identity.domain.port.in.UserView;
import com.condor.nexussoft.timeclock.identity.domain.port.out.PasswordHasherPort;
import com.condor.nexussoft.timeclock.identity.domain.port.out.UserAdminRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class UserManagementService implements UserManagementUseCase {

    private final UserAdminRepositoryPort users;
    private final PasswordHasherPort passwordHasher;

    public UserManagementService(UserAdminRepositoryPort users, PasswordHasherPort passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public UserView create(UUID tenantId, UserCommands.CreateUserCommand command) {
        String email = Email.of(command.email()).value();
        if (users.existsByTenantAndEmail(tenantId, email)) {
            throw new DomainException("DUPLICATE_EMAIL", "Ya existe un usuario con el correo " + email);
        }
        String passwordHash = passwordHasher.hash(command.password());
        Set<String> roles = command.roleCodes() == null ? Set.of() : command.roleCodes();
        return users.create(tenantId, email, passwordHash, command.firstName(),
                command.lastName(), command.employeeCode(), roles);
    }

    @Override
    @Transactional(readOnly = true)
    public UserView get(UUID tenantId, UUID userId) {
        return users.findByIdAndTenant(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<UserView> list(UUID tenantId, int page, int size, String search) {
        return users.findAllByTenant(tenantId, page, size, search);
    }

    @Override
    @Transactional
    public UserView updateStatus(UUID tenantId, UUID userId, String status) {
        return users.updateStatus(userId, tenantId, status)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }

    @Override
    @Transactional
    public UserView assignRoles(UUID tenantId, UUID userId, Set<String> roleCodes) {
        return users.assignRoles(userId, tenantId, roleCodes)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }
}
