package com.condor.nexussoft.timeclock.geofencing.domain.port.in;

import com.condor.nexussoft.timeclock.geofencing.domain.Geofence;
import com.condor.nexussoft.timeclock.geofencing.domain.QrPayload;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Geocercas por centro y ciclo de vida del QR firmado (RF-10, RF-14). */
public interface GeofencingUseCase {

    /** QR generado devuelto al administrador: cadena firmada + vigencia. */
    record GeneratedQr(String token, Instant expiresAt) {
    }

    Geofence upsertGeofence(UUID tenantId, UUID workSiteId, double latitude, double longitude, double radiusM);

    Geofence getGeofence(UUID tenantId, UUID workSiteId);

    /**
     * Búsqueda no excepcional de la geocerca activa del centro. Devuelve vacío si no hay
     * geocerca configurada (condición de negocio normal, no un error). Pensada para flujos
     * que participan en una transacción externa —p. ej. el registro de asistencia—, donde
     * lanzar una excepción marcaría la transacción como rollback-only.
     */
    Optional<Geofence> findGeofence(UUID tenantId, UUID workSiteId);

    GeneratedQr generateQr(UUID tenantId, UUID workSiteId);

    /** Verifica firma y vigencia del QR; el consumo del nonce (anti-replay) ocurre al registrar (BC-06). */
    QrPayload verifyQr(String token);
}
