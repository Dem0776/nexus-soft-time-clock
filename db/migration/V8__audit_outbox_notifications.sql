-- =====================================================================
-- V8 — Audit (BC-10), Outbox (event-driven) y Notifications (BC-12)
-- =====================================================================

-- =====================================================================
-- audit_logs — bitácora inmutable, PARTICIONADA por created_at (RN-60/61)
-- =====================================================================
CREATE TABLE audit_logs (
    id            uuid        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     uuid,                                   -- NULL para acciones de plataforma
    created_at    timestamptz NOT NULL DEFAULT now(),     -- clave de partición
    actor_user_id uuid,
    actor_email   varchar(255),
    action        varchar(80) NOT NULL,                   -- LOGIN, CREATE_USER, UPDATE_GEOFENCE, ...
    resource_type varchar(60),
    resource_id   varchar(64),
    ip            inet,
    user_agent    varchar(400),
    device_info   varchar(200),
    old_values    jsonb,
    new_values    jsonb,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE INDEX ix_audit_tenant_time   ON audit_logs (tenant_id, created_at DESC);
CREATE INDEX ix_audit_actor         ON audit_logs (actor_user_id, created_at DESC);
CREATE INDEX ix_audit_action        ON audit_logs (action);
CREATE INDEX gix_audit_new_values   ON audit_logs USING gin (new_values);

CREATE TABLE audit_logs_2026_07 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE audit_logs_2026_08 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;

-- Inmutabilidad (RN-61): impedir UPDATE/DELETE por triggers.
CREATE OR REPLACE FUNCTION fn_block_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Tabla append-only: operación % no permitida', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_no_update BEFORE UPDATE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION fn_block_mutation();
CREATE TRIGGER trg_audit_no_delete BEFORE DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION fn_block_mutation();

COMMENT ON TABLE audit_logs IS 'Bitácora inmutable (append-only). Cada acción de escritura genera un registro (RN-60).';

-- =====================================================================
-- outbox_events — Transactional Outbox (ADR-005)
-- =====================================================================
CREATE TABLE outbox_events (
    id             uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      uuid,
    aggregate_type varchar(60) NOT NULL,   -- 'AttendanceRecord', 'User', ...
    aggregate_id   varchar(64) NOT NULL,
    event_type     varchar(80) NOT NULL,   -- 'AttendanceRegistered', ...
    payload        jsonb       NOT NULL,
    occurred_at    timestamptz NOT NULL DEFAULT now(),
    published_at   timestamptz,
    attempts       integer     NOT NULL DEFAULT 0,
    status         varchar(15) NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','PUBLISHED','FAILED'))
);

-- El relay consulta pendientes en orden de ocurrencia.
CREATE INDEX ix_outbox_pending ON outbox_events (occurred_at) WHERE status = 'PENDING';
CREATE INDEX ix_outbox_tenant  ON outbox_events (tenant_id, occurred_at DESC);

COMMENT ON TABLE outbox_events IS 'Outbox transaccional para publicar eventos de dominio de forma consistente (ADR-005).';

-- =====================================================================
-- notifications — envíos push/email (BC-12)
-- =====================================================================
CREATE TABLE notifications (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id      uuid        REFERENCES users (id) ON DELETE CASCADE,
    channel      varchar(10) NOT NULL CHECK (channel IN ('PUSH','EMAIL','INAPP')),
    type         varchar(60) NOT NULL,
    title        varchar(200),
    body         varchar(1000),
    payload      jsonb       NOT NULL DEFAULT '{}'::jsonb,
    status       varchar(15) NOT NULL DEFAULT 'PENDING'
                 CHECK (status IN ('PENDING','SENT','FAILED','READ')),
    sent_at      timestamptz,
    read_at      timestamptz,
    created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_notifications_user   ON notifications (tenant_id, user_id, created_at DESC);
CREATE INDEX ix_notifications_status ON notifications (status) WHERE status = 'PENDING';
