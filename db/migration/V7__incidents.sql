-- =====================================================================
-- V7 — Incidents (BC-09): incidencias y justificaciones
-- =====================================================================

CREATE TABLE incidents (
    id                     uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id                uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type                   varchar(25) NOT NULL
                           CHECK (type IN ('RETARDO','FALTA','REGISTRO_RECHAZADO','PERMISO',
                                           'JUSTIFICACION','FRAUDE','OTRO')),
    status                 varchar(20) NOT NULL DEFAULT 'OPEN'
                           CHECK (status IN ('OPEN','APPROVED','REJECTED','RESOLVED')),
    priority               varchar(10) NOT NULL DEFAULT 'MEDIUM'
                           CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    incident_date          date        NOT NULL DEFAULT current_date,
    related_attendance_id  uuid,                         -- referencia lógica (tabla particionada)
    related_server_time    timestamptz,
    work_site_id           uuid        REFERENCES work_sites (id) ON DELETE SET NULL,
    description            varchar(1000),
    evidence_bucket        varchar(120),
    evidence_key           varchar(300),
    resolution_note        varchar(1000),
    resolved_by            uuid        REFERENCES users (id) ON DELETE SET NULL,
    resolved_at            timestamptz,
    created_at             timestamptz NOT NULL DEFAULT now(),
    updated_at             timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_incidents_tenant_status ON incidents (tenant_id, status);
CREATE INDEX ix_incidents_tenant_user   ON incidents (tenant_id, user_id, incident_date DESC);
CREATE INDEX ix_incidents_tenant_type   ON incidents (tenant_id, type);

CREATE TRIGGER trg_incidents_updated_at BEFORE UPDATE ON incidents
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE incidents IS 'Incidencias: retardos, faltas, registros rechazados, permisos, justificaciones (BC-09).';
