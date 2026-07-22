package com.condor.nexussoft.timeclock.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de un evento de dominio. Los eventos concretos (p.ej. AttendanceRegistered)
 * viven en su bounded context pero implementan esta interfaz estable del shared-kernel.
 */
public interface DomainEvent {

    /** Identificador único del evento (idempotencia de consumidores). */
    UUID eventId();

    /** Momento en que ocurrió el hecho de negocio (hora de servidor, ADR-003). */
    Instant occurredAt();

    /** Tipo lógico del evento, p.ej. "AttendanceRegistered". */
    String eventType();

    /** Tenant al que pertenece el evento (aislamiento multi-tenant, ADR-002). Puede ser null en eventos de plataforma. */
    UUID tenantId();
}
