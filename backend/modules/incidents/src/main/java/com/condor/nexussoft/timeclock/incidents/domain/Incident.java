package com.condor.nexussoft.timeclock.incidents.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Incidencia (RF-09): situación que requiere atención (retardo, falta, rechazo, permiso…). */
public class Incident {

    public enum Type { RETARDO, FALTA, REGISTRO_RECHAZADO, PERMISO, JUSTIFICACION, FRAUDE, OTRO }

    public enum Status { OPEN, APPROVED, REJECTED, RESOLVED }

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final Type type;
    private Status status;
    private final String priority;
    private final LocalDate incidentDate;
    private final UUID relatedAttendanceId;
    private final String description;
    private String resolutionNote;
    private UUID resolvedBy;
    private Instant resolvedAt;

    public Incident(UUID id, UUID tenantId, UUID userId, Type type, Status status, String priority,
                    LocalDate incidentDate, UUID relatedAttendanceId, String description,
                    String resolutionNote, UUID resolvedBy, Instant resolvedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.priority = priority;
        this.incidentDate = incidentDate;
        this.relatedAttendanceId = relatedAttendanceId;
        this.description = description;
        this.resolutionNote = resolutionNote;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }

    public static Incident openForRejectedAttendance(UUID tenantId, UUID userId, UUID attendanceId, String reason) {
        return new Incident(UUID.randomUUID(), tenantId, userId, Type.REGISTRO_RECHAZADO, Status.OPEN,
                "MEDIUM", LocalDate.now(), attendanceId, "Registro rechazado: " + reason,
                null, null, null);
    }

    /** Resuelve la incidencia (aprobar/rechazar/resolver). Debe quedar auditado (RN-43). */
    public void resolve(Status newStatus, String note, UUID resolverId, Instant now) {
        if (this.status != Status.OPEN) {
            throw new IllegalStateException("La incidencia ya fue resuelta");
        }
        this.status = newStatus;
        this.resolutionNote = note;
        this.resolvedBy = resolverId;
        this.resolvedAt = now;
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID userId() { return userId; }
    public Type type() { return type; }
    public Status status() { return status; }
    public String priority() { return priority; }
    public LocalDate incidentDate() { return incidentDate; }
    public UUID relatedAttendanceId() { return relatedAttendanceId; }
    public String description() { return description; }
    public String resolutionNote() { return resolutionNote; }
    public UUID resolvedBy() { return resolvedBy; }
    public Instant resolvedAt() { return resolvedAt; }
}
