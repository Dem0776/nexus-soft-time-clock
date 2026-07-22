package com.condor.nexussoft.timeclock.identity.domain.event;

import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Evento: un usuario inició sesión correctamente. Consumido por Audit y Notifications. */
public record UserLoggedIn(UUID eventId, Instant occurredAt, UUID tenantId, UUID userId)
        implements DomainEvent {

    public static UserLoggedIn now(UUID userId, UUID tenantId, Instant when) {
        return new UserLoggedIn(UUID.randomUUID(), when, tenantId, userId);
    }

    @Override
    public String eventType() {
        return "UserLoggedIn";
    }
}
