package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.util.UUID;

/** Verifica el QR firmado del centro (delegado al BC Geofencing). */
public interface QrValidationPort {

    record QrCheck(boolean valid, boolean expired, UUID tenantId, UUID workSiteId, String nonce) {
        public static QrCheck invalid() {
            return new QrCheck(false, false, null, null, null);
        }
    }

    QrCheck verify(String qrToken);
}
