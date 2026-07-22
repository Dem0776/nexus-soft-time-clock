package com.condor.nexussoft.timeclock.scheduling.domain;

import java.time.LocalDate;
import java.util.UUID;

/** Asignación de un turno a un colaborador con vigencia (RF-08). */
public class ShiftAssignment {

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final UUID shiftId;
    private final UUID workSiteId;
    private final LocalDate validFrom;
    private final LocalDate validTo;

    public ShiftAssignment(UUID id, UUID tenantId, UUID userId, UUID shiftId, UUID workSiteId,
                           LocalDate validFrom, LocalDate validTo) {
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo no puede ser anterior a validFrom");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.shiftId = shiftId;
        this.workSiteId = workSiteId;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public static ShiftAssignment create(UUID tenantId, UUID userId, UUID shiftId, UUID workSiteId,
                                         LocalDate validFrom, LocalDate validTo) {
        return new ShiftAssignment(UUID.randomUUID(), tenantId, userId, shiftId, workSiteId, validFrom, validTo);
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID userId() { return userId; }
    public UUID shiftId() { return shiftId; }
    public UUID workSiteId() { return workSiteId; }
    public LocalDate validFrom() { return validFrom; }
    public LocalDate validTo() { return validTo; }
}
