package com.condor.nexussoft.timeclock.attendance.domain.event;

import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Evento: se rechazó un registro de asistencia (con motivo). Consumido por Audit, Notifications, Incidents. */
public record AttendanceRejected(UUID eventId, Instant occurredAt, UUID tenantId,
                                 UUID attendanceId, UUID userId, String reason)
        implements DomainEvent {

    public static AttendanceRejected of(UUID tenantId, UUID attendanceId, UUID userId, String reason, Instant when) {
        return new AttendanceRejected(UUID.randomUUID(), when, tenantId, attendanceId, userId, reason);
    }

    @Override
    public String eventType() {
        return "AttendanceRejected";
    }
}
