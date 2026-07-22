package com.condor.nexussoft.timeclock.sync.domain.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resultado por operación en una sincronización. {@code error} no nulo indica que el item
 * no pudo procesarse (se reintentará); en caso contrario {@code status} refleja la decisión
 * del servidor (ACCEPTED/REJECTED) — resolución de conflictos autoritativa (RN-53).
 */
public record SyncItemResult(
        UUID operationUuid,
        String status,
        String rejectionReason,
        Instant serverTime,
        Double distanceToSiteM,
        List<String> flags,
        String error) {
}
