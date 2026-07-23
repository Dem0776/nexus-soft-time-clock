-- =====================================================================
-- V13 — Nuevos motivos de rechazo por política de centro
-- HU-13 CA1 (foto obligatoria) y HU-14 CA1 (biometría obligatoria):
-- el registro se rechaza si el centro exige foto/biometría y no se aporta.
-- Amplía el CHECK de attendance_records.rejection_reason con
-- PHOTO_REQUIRED y BIOMETRIC_REQUIRED.
-- =====================================================================

ALTER TABLE attendance_records
    DROP CONSTRAINT IF EXISTS attendance_records_rejection_reason_check;

ALTER TABLE attendance_records
    ADD CONSTRAINT attendance_records_rejection_reason_check
    CHECK (rejection_reason IS NULL OR rejection_reason IN (
        'INVALID_QR','OUT_OF_GEOFENCE','LOW_GPS_ACCURACY','GPS_UNAVAILABLE',
        'OUT_OF_SCHEDULE','FRAUD_MOCK_LOCATION','FRAUD_ROOTED_DEVICE',
        'FRAUD_GPS_SPOOF_APP','REPLAY_DETECTED','INVALID_SEQUENCE','UNTRUSTED_DEVICE',
        'PHOTO_REQUIRED','BIOMETRIC_REQUIRED'));
