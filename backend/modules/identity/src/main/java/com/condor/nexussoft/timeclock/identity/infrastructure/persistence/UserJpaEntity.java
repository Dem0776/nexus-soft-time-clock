package com.condor.nexussoft.timeclock.identity.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "is_platform_admin", nullable = false)
    private boolean platformAdmin;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(nullable = false)
    private String status;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleJpaEntity> roles = new HashSet<>();

    protected UserJpaEntity() {
    }

    /** Constructor de creación administrativa (usuario nuevo dentro de un tenant). */
    public UserJpaEntity(UUID id, UUID tenantId, boolean platformAdmin, String email, String passwordHash,
                         String firstName, String lastName, String employeeCode, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.platformAdmin = platformAdmin;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeCode = employeeCode;
        this.status = status;
        this.failedLoginCount = 0;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public boolean isPlatformAdmin() { return platformAdmin; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmployeeCode() { return employeeCode; }
    public String getStatus() { return status; }
    public int getFailedLoginCount() { return failedLoginCount; }
    public Instant getLockedUntil() { return lockedUntil; }
    public Set<RoleJpaEntity> getRoles() { return roles; }

    public void setStatus(String status) { this.status = status; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
    public void setRoles(Set<RoleJpaEntity> roles) { this.roles = roles; }
}
