package com.condor.nexussoft.timeclock.attendance.domain.event;

import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Evento: se aceptó un registro de asistencia. Consumido por Audit, Notifications, Reporting, Realtime. */
public record AttendanceRegistered(UUID eventId, Instant occurredAt, UUID tenantId,
                                   UUID attendanceId, UUID userId, UUID workSiteId, String eventKind)
        implements DomainEvent {

    public static AttendanceRegistered of(UUID tenantId, UUID attendanceId, UUID userId,
                                          UUID workSiteId, String eventKind, Instant when) {
        return new AttendanceRegistered(UUID.randomUUID(), when, tenantId, attendanceId, userId, workSiteId, eventKind);
    }

    @Override
    public String eventType() {
        return "AttendanceRegistered";
    }
}
