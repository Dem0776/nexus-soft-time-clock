package com.condor.nexussoft.timeclock.geofencing.domain;

import java.time.Instant;
import java.util.UUID;

/** Contenido firmado de un QR de centro. */
public record QrPayload(UUID tenantId, UUID workSiteId, String nonce, Instant expiresAt) {
}
