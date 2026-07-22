package com.condor.nexussoft.timeclock.audit.domain;

import java.time.Instant;
import java.util.UUID;

/** Entrada inmutable de la bitácora (RN-60, RN-61). */
public record AuditLogEntry(
        UUID id,
        UUID tenantId,
        UUID actorUserId,
        String action,
        String resourceType,
        String resourceId,
        String newValuesJson,
        Instant createdAt) {
}
