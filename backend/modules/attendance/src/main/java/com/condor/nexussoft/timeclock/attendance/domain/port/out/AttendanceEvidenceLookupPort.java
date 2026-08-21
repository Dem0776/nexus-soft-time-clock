package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Localiza la referencia de evidencia de un registro para mostrarla en el portal (HU-13 CA2). */
public interface AttendanceEvidenceLookupPort {

    /**
     * @param serverTime hora del registro, si el llamador la conoce. {@code attendance_records} está
     *                   particionada por {@code server_time}, así que aportarla acota la búsqueda a
     *                   una partición en vez de recorrerlas todas.
     */
    Optional<String> findEvidenceKey(UUID tenantId, UUID recordId, Instant serverTime);
}
