package com.condor.nexussoft.timeclock.audit.application;

import com.condor.nexussoft.timeclock.audit.domain.AuditLogEntry;
import com.condor.nexussoft.timeclock.audit.domain.port.out.AuditLogRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Convierte un evento de dominio en una entrada de auditoría y la persiste (append-only). */
@Service
public class AuditRecorder {

    private final AuditLogRepositoryPort auditLog;
    private final ObjectMapper objectMapper;

    public AuditRecorder(AuditLogRepositoryPort auditLog, ObjectMapper objectMapper) {
        this.auditLog = auditLog;
        this.objectMapper = objectMapper;
    }

    public void record(DomainEvent event) {
        auditLog.append(new AuditLogEntry(
                UUID.randomUUID(),
                event.tenantId(),
                currentActor(),
                event.eventType(),
                event.getClass().getSimpleName(),
                event.eventId().toString(),
                toJson(event),
                event.occurredAt()));
    }

    /** Actor de la petición si el evento se emite en el hilo de un request autenticado. */
    private UUID currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String toJson(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return "{}";
        }
    }
}
