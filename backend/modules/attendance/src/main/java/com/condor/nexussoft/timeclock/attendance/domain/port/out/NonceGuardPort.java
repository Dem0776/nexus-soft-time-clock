package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.util.UUID;

/** Consumo de nonce del QR para anti-replay (RN-26). */
public interface NonceGuardPort {

    /** Intenta consumir el nonce; devuelve {@code false} si ya había sido consumido (replay). */
    boolean tryConsume(UUID tenantId, UUID workSiteId, String nonce, UUID attendanceId);
}
