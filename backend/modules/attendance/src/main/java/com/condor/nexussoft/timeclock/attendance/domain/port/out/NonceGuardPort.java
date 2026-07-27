package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consumo de nonce del QR para anti-replay (RN-26). Un QR de vigencia larga puede ser
 * escaneado por distintos usuarios, y por el mismo usuario en días distintos; la barrera es
 * "una vez por usuario y por día calendario" ({@code consumedOn}), no un solo uso total.
 */
public interface NonceGuardPort {

    /** Intenta consumir el nonce para ese usuario y día; devuelve {@code false} si ya fue consumido (replay). */
    boolean tryConsume(UUID tenantId, UUID workSiteId, String nonce, UUID userId, LocalDate consumedOn, UUID attendanceId);
}
