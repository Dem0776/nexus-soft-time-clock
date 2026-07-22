-- =====================================================================
-- V4 — Scheduling (BC-04): horarios, turnos y asignaciones
-- =====================================================================

CREATE TABLE schedules (
    id          uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   uuid         NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    code        varchar(40)  NOT NULL,
    name        varchar(200) NOT NULL,
    timezone    varchar(64),                     -- override; si NULL hereda del centro/tenant
    config_json jsonb        NOT NULL DEFAULT '{}'::jsonb,  -- días laborables, reglas específicas
    status      varchar(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_schedules_tenant_code ON schedules (tenant_id, code);

CREATE TRIGGER trg_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Turnos (definición horaria dentro de un horario)
CREATE TABLE shifts (
    id                  uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    schedule_id         uuid        NOT NULL REFERENCES schedules (id) ON DELETE CASCADE,
    name                varchar(120) NOT NULL,
    start_time          time        NOT NULL,
    end_time            time        NOT NULL,
    crosses_midnight    boolean     NOT NULL DEFAULT false,   -- turno nocturno
    break_minutes       integer     NOT NULL DEFAULT 0  CHECK (break_minutes >= 0),
    late_tolerance_min  integer     NOT NULL DEFAULT 10 CHECK (late_tolerance_min >= 0),   -- RN-16
    early_tolerance_min integer     NOT NULL DEFAULT 10 CHECK (early_tolerance_min >= 0),  -- ventana previa (RN-15)
    window_before_min   integer     NOT NULL DEFAULT 30 CHECK (window_before_min >= 0),
    window_after_min    integer     NOT NULL DEFAULT 30 CHECK (window_after_min >= 0),
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_shifts_tenant_schedule ON shifts (tenant_id, schedule_id);

CREATE TRIGGER trg_shifts_updated_at BEFORE UPDATE ON shifts
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Asignación de turno a colaborador (con vigencia)
CREATE TABLE shift_assignments (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id      uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    shift_id     uuid        NOT NULL REFERENCES shifts (id) ON DELETE CASCADE,
    work_site_id uuid        REFERENCES work_sites (id) ON DELETE SET NULL,
    valid_from   date        NOT NULL,
    valid_to     date,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_shift_assign_range CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX ix_shift_assign_user_valid ON shift_assignments (tenant_id, user_id, valid_from, valid_to);

CREATE TRIGGER trg_shift_assignments_updated_at BEFORE UPDATE ON shift_assignments
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
