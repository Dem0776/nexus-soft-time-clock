package com.condor.nexussoft.timeclock.identity.domain.model;

import com.condor.nexussoft.timeclock.identity.domain.event.UserLockedOut;
import com.condor.nexussoft.timeclock.identity.domain.event.UserLoggedIn;
import com.condor.nexussoft.timeclock.shared.domain.AggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agregado raíz de usuario. Encapsula las invariantes de bloqueo por intentos
 * fallidos (RN-40) y la pertenencia a un único tenant salvo super admin (RN-32).
 */
public class User extends AggregateRoot<UUID> {

    private final UUID id;
    private final UUID tenantId;          // null solo para super admin de plataforma
    private final boolean platformAdmin;
    private final Email email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private UserStatus status;
    private int failedLoginCount;
    private Instant lockedUntil;
    private final Set<Role> roles;

    public User(UUID id, UUID tenantId, boolean platformAdmin, Email email, String passwordHash,
                String firstName, String lastName, UserStatus status, int failedLoginCount,
                Instant lockedUntil, Set<Role> roles) {
        this.id = id;
        this.tenantId = tenantId;
        this.platformAdmin = platformAdmin;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.failedLoginCount = failedLoginCount;
        this.lockedUntil = lockedUntil;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public boolean isLocked(Instant now) {
        return status == UserStatus.LOCKED && lockedUntil != null && lockedUntil.isAfter(now);
    }

    public boolean canAuthenticate(Instant now) {
        return status == UserStatus.ACTIVE && !isLocked(now);
    }

    /** Registra un login exitoso: limpia contadores y emite el evento de dominio. */
    public void registerSuccessfulLogin(Instant now) {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
        registerEvent(UserLoggedIn.now(id, tenantId, now));
    }

    /** Registra un intento fallido; bloquea al superar el máximo (RN-40). */
    public void registerFailedLogin(int maxAttempts, Duration lockDuration, Instant now) {
        this.failedLoginCount++;
        if (this.failedLoginCount >= maxAttempts) {
            this.status = UserStatus.LOCKED;
            this.lockedUntil = now.plus(lockDuration);
            registerEvent(UserLockedOut.now(id, tenantId, now));
        }
    }

    public Set<String> permissionCodes() {
        return roles.stream()
                .flatMap(r -> r.permissions().stream())
                .map(Permission::code)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> roleCodes() {
        return roles.stream().map(Role::code).collect(Collectors.toUnmodifiableSet());
    }

    public UUID tenantId()       { return tenantId; }
    public boolean platformAdmin() { return platformAdmin; }
    public Email email()         { return email; }
    public String passwordHash() { return passwordHash; }
    public String firstName()    { return firstName; }
    public String lastName()     { return lastName; }
    public UserStatus status()   { return status; }
    public int failedLoginCount() { return failedLoginCount; }
    public Instant lockedUntil() { return lockedUntil; }
    public Set<Role> roles()     { return roles; }
}
