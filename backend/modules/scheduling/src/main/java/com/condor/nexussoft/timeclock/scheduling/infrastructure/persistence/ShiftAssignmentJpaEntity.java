package com.condor.nexussoft.timeclock.scheduling.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shift_assignments")
public class ShiftAssignmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "shift_id", nullable = false)
    private UUID shiftId;

    @Column(name = "work_site_id")
    private UUID workSiteId;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    protected ShiftAssignmentJpaEntity() {
    }

    public ShiftAssignmentJpaEntity(UUID id, UUID tenantId, UUID userId, UUID shiftId, UUID workSiteId,
                                    LocalDate validFrom, LocalDate validTo) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.shiftId = shiftId;
        this.workSiteId = workSiteId;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public UUID getShiftId() { return shiftId; }
    public UUID getWorkSiteId() { return workSiteId; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidTo() { return validTo; }
}
