package com.condor.nexussoft.timeclock.identity.domain.event;

import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Evento: cuenta bloqueada por superar el máximo de intentos fallidos (RN-40). */
public record UserLockedOut(UUID eventId, Instant occurredAt, UUID tenantId, UUID userId)
        implements DomainEvent {

    public static UserLockedOut now(UUID userId, UUID tenantId, Instant when) {
        return new UserLockedOut(UUID.randomUUID(), when, tenantId, userId);
    }

    @Override
    public String eventType() {
        return "UserLockedOut";
    }
}
