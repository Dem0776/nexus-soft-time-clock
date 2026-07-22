-- =====================================================================
-- V6 — Attendance (BC-06, núcleo) + Anti-Fraud (BC-07) + Sync (BC-08)
-- Tabla particionada por rango de tiempo (server_time) para escalar a
-- millones de registros (RNF-03).
-- =====================================================================

-- =====================================================================
-- attendance_records — PARTICIONADA POR RANGO (server_time)
-- La PK incluye la clave de partición (requisito de PostgreSQL).
-- =====================================================================
CREATE TABLE attendance_records (
    id                  uuid                    NOT NULL DEFAULT gen_random_uuid(),
    tenant_id           uuid                    NOT NULL,
    server_time         timestamptz             NOT NULL,        -- hora oficial (RN-11) y clave de partición
    user_id             uuid                    NOT NULL,
    work_site_id        uuid                    NOT NULL,
    geofence_id         uuid,
    shift_id            uuid,
    project_id          uuid,
    event_type          varchar(20)             NOT NULL
                        CHECK (event_type IN ('ENTRADA','SALIDA','INICIO_DESCANSO','FIN_DESCANSO','CAMBIO_SITIO')),
    status              varchar(20)             NOT NULL
                        CHECK (status IN ('ACCEPTED','REJECTED','PENDING_REVIEW')),
    rejection_reason    varchar(40)             -- OUT_OF_GEOFENCE, LOW_GPS_ACCURACY, INVALID_QR, etc.
                        CHECK (rejection_reason IS NULL OR rejection_reason IN (
                            'INVALID_QR','OUT_OF_GEOFENCE','LOW_GPS_ACCURACY','GPS_UNAVAILABLE',
                            'OUT_OF_SCHEDULE','FRAUD_MOCK_LOCATION','FRAUD_ROOTED_DEVICE',
                            'FRAUD_GPS_SPOOF_APP','REPLAY_DETECTED','INVALID_SEQUENCE','UNTRUSTED_DEVICE')),
    location            geography(Point, 4326)  NOT NULL,
    gps_accuracy_m      numeric(8,2)            NOT NULL CHECK (gps_accuracy_m >= 0),
    distance_to_site_m  numeric(10,2),
    device_id           uuid,
    device_time         timestamptz,                             -- metadato no autoritativo (ADR-003)
    time_skew_seconds   integer,                                 -- server_time - device_time
    qr_token_id         uuid,
    operation_uuid      uuid                    NOT NULL,         -- idempotencia/offline (ADR-004)
    source              varchar(15)             NOT NULL DEFAULT 'ONLINE'
                        CHECK (source IN ('ONLINE','OFFLINE_SYNC')),
    biometric_verified  boolean                 NOT NULL DEFAULT false,
    evidence_bucket     varchar(120),
    evidence_key        varchar(300),
    evidence_hash       varchar(128),
    validations_json    jsonb                   NOT NULL DEFAULT '{}'::jsonb,  -- detalle de cada validación
    created_at          timestamptz             NOT NULL DEFAULT now(),
    PRIMARY KEY (id, server_time)
) PARTITION BY RANGE (server_time);

-- Índices (se propagan a las particiones)
CREATE INDEX ix_att_tenant_user_time ON attendance_records (tenant_id, user_id, server_time DESC);
CREATE INDEX ix_att_tenant_site_time ON attendance_records (tenant_id, work_site_id, server_time DESC);
CREATE INDEX ix_att_tenant_status    ON attendance_records (tenant_id, status);
CREATE INDEX gix_att_location        ON attendance_records USING gist (location);

COMMENT ON TABLE attendance_records IS 'Registros de asistencia (núcleo). Particionada por server_time (RNF-03).';

-- Particiones iniciales (un job crea las futuras con antelación).
CREATE TABLE attendance_records_2026_07 PARTITION OF attendance_records
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE attendance_records_2026_08 PARTITION OF attendance_records
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE attendance_records_2026_09 PARTITION OF attendance_records
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
-- Partición por defecto: evita fallos de inserción fuera de rango conocido.
CREATE TABLE attendance_records_default PARTITION OF attendance_records DEFAULT;

-- =====================================================================
-- idempotency_keys — registro de operaciones procesadas (ADR-004)
-- No particionada; unicidad global por (tenant, operation_uuid).
-- =====================================================================
CREATE TABLE idempotency_keys (
    id                     uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    operation_uuid         uuid        NOT NULL,
    endpoint               varchar(120) NOT NULL,
    request_hash           varchar(128),
    response_status        integer,
    attendance_id          uuid,
    attendance_server_time timestamptz,
    created_at             timestamptz NOT NULL DEFAULT now(),
    expires_at             timestamptz NOT NULL
);

CREATE UNIQUE INDEX uq_idempotency_tenant_op ON idempotency_keys (tenant_id, operation_uuid);
CREATE INDEX        ix_idempotency_expires    ON idempotency_keys (expires_at);

-- =====================================================================
-- fraud_flags — banderas antifraude por registro (RN-28)
-- FK compuesta hacia la tabla particionada.
-- =====================================================================
CREATE TABLE fraud_flags (
    id                     uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    attendance_id          uuid        NOT NULL,
    attendance_server_time timestamptz NOT NULL,
    flag_type              varchar(30) NOT NULL
                           CHECK (flag_type IN ('MOCK_LOCATION','ROOTED_DEVICE','JAILBREAK',
                                                'GPS_SPOOF_APP','LOW_ACCURACY','GPS_DISABLED',
                                                'REPLAY','UNTRUSTED_DEVICE','TIME_SKEW')),
    is_blocking            boolean     NOT NULL DEFAULT false,
    details_json           jsonb       NOT NULL DEFAULT '{}'::jsonb,
    created_at             timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_fraud_flags_attendance
        FOREIGN KEY (attendance_id, attendance_server_time)
        REFERENCES attendance_records (id, server_time) ON DELETE CASCADE
);

CREATE INDEX ix_fraud_flags_tenant_type ON fraud_flags (tenant_id, flag_type);
CREATE INDEX ix_fraud_flags_attendance  ON fraud_flags (attendance_id);

-- =====================================================================
-- work_days — jornada agregada (read-model de horas trabajadas/extra)
-- =====================================================================
CREATE TABLE work_days (
    id                     uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id                uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    work_date              date        NOT NULL,
    work_site_id           uuid        REFERENCES work_sites (id) ON DELETE SET NULL,
    shift_id               uuid        REFERENCES shifts (id) ON DELETE SET NULL,
    entry_time             timestamptz,
    exit_time              timestamptz,
    worked_minutes         integer     NOT NULL DEFAULT 0,
    break_minutes          integer     NOT NULL DEFAULT 0,
    overtime_minutes       integer     NOT NULL DEFAULT 0,   -- RN-17
    late_minutes           integer     NOT NULL DEFAULT 0,   -- RN-16
    status                 varchar(20) NOT NULL DEFAULT 'OPEN'
                           CHECK (status IN ('OPEN','CLOSED','INCOMPLETE')),
    created_at             timestamptz NOT NULL DEFAULT now(),
    updated_at             timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_work_days_user_date ON work_days (tenant_id, user_id, work_date);
CREATE INDEX        ix_work_days_tenant_date ON work_days (tenant_id, work_date);

CREATE TRIGGER trg_work_days_updated_at BEFORE UPDATE ON work_days
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
