package com.condor.nexussoft.timeclock.incidents.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class IncidentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String priority;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "related_attendance_id")
    private UUID relatedAttendanceId;

    private String description;

    @Column(name = "resolution_note")
    private String resolutionNote;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IncidentJpaEntity() {
    }

    public IncidentJpaEntity(UUID id, UUID tenantId, UUID userId, String type, String status, String priority,
                             LocalDate incidentDate, UUID relatedAttendanceId, String description,
                             String resolutionNote, UUID resolvedBy, Instant resolvedAt, Instant createdAt) {
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
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public UUID getRelatedAttendanceId() { return relatedAttendanceId; }
    public String getDescription() { return description; }
    public String getResolutionNote() { return resolutionNote; }
    public UUID getResolvedBy() { return resolvedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public void applyResolution(String status, String resolutionNote, UUID resolvedBy, Instant resolvedAt) {
        this.status = status;
        this.resolutionNote = resolutionNote;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }
}
