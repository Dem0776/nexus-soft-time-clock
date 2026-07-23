-- =====================================================================
-- V14 — Tipos de evento configurables por empresa (HU-12 CA1)
-- Catálogo por tenant de los tipos de evento INTERMEDIOS (descansos y
-- cambio de sitio): habilitación y etiqueta. ENTRADA/SALIDA son núcleo
-- y no se configuran. Añade el motivo de rechazo EVENT_TYPE_DISABLED.
-- =====================================================================

CREATE TABLE attendance_event_type_settings (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    event_type   varchar(20) NOT NULL
                 CHECK (event_type IN ('INICIO_DESCANSO','FIN_DESCANSO','CAMBIO_SITIO')),
    enabled      boolean     NOT NULL DEFAULT true,
    label        varchar(60),
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_att_event_type_tenant
    ON attendance_event_type_settings (tenant_id, event_type);

COMMENT ON TABLE attendance_event_type_settings IS
    'Configuración por empresa de los tipos de evento intermedios (HU-12 CA1).';

-- Nuevo motivo de rechazo: el tipo de evento está deshabilitado para la empresa.
ALTER TABLE attendance_records
    DROP CONSTRAINT IF EXISTS attendance_records_rejection_reason_check;

ALTER TABLE attendance_records
    ADD CONSTRAINT attendance_records_rejection_reason_check
    CHECK (rejection_reason IS NULL OR rejection_reason IN (
        'INVALID_QR','OUT_OF_GEOFENCE','LOW_GPS_ACCURACY','GPS_UNAVAILABLE',
        'OUT_OF_SCHEDULE','FRAUD_MOCK_LOCATION','FRAUD_ROOTED_DEVICE',
        'FRAUD_GPS_SPOOF_APP','REPLAY_DETECTED','INVALID_SEQUENCE','UNTRUSTED_DEVICE',
        'PHOTO_REQUIRED','BIOMETRIC_REQUIRED','EVENT_TYPE_DISABLED'));
