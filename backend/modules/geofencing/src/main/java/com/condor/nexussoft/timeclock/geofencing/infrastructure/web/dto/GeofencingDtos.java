package com.condor.nexussoft.timeclock.geofencing.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.geofencing.domain.Geofence;
import com.condor.nexussoft.timeclock.geofencing.domain.port.in.GeofencingUseCase;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public final class GeofencingDtos {

    private GeofencingDtos() {
    }

    public record GeofenceRequest(
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @NotNull @Positive Double radiusM) {
    }

    public record GeofenceResponse(UUID workSiteId, double latitude, double longitude,
                                   double radiusM, boolean active) {
        public static GeofenceResponse from(Geofence g) {
            return new GeofenceResponse(g.workSiteId(), g.center().latitude(), g.center().longitude(),
                    g.radiusM(), g.active());
        }
    }

    /** Vigencia del QR en minutos; {@code null} aplica la duración por defecto configurada. */
    public record QrRequest(@Min(1) @Max(1440) Integer ttlMinutes) {
    }

    public record QrResponse(String token, Instant expiresAt) {
        public static QrResponse from(GeofencingUseCase.GeneratedQr qr) {
            return new QrResponse(qr.token(), qr.expiresAt());
        }
    }
}
