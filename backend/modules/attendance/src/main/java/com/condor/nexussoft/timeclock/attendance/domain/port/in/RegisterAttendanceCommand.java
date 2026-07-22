package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import java.util.UUID;

/**
 * Comando de registro de asistencia. Incluye la operación idempotente (UUID cliente),
 * el QR firmado, la lectura GPS, el tipo de evento, y las señales antifraude del dispositivo.
 */
public record RegisterAttendanceCommand(
        UUID operationUuid,
        UUID workSiteId,
        String qrToken,
        double latitude,
        double longitude,
        double accuracyM,
        String eventType,
        String deviceId,
        Long deviceTimeEpochMs,
        String source,
        boolean mockLocation,
        boolean rootedOrJailbroken,
        boolean gpsSpoofApp,
        boolean gpsDisabled,
        boolean deviceTrusted,
        boolean biometricVerified,
        String evidenceBucket,
        String evidenceKey,
        String evidenceHash) {
}
