-- =====================================================================
-- V3 — Organization (BC-03): centros de trabajo y proyectos
-- =====================================================================

CREATE TABLE work_sites (
    id                 uuid                    PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          uuid                    NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    code               varchar(40)             NOT NULL,
    name               varchar(200)            NOT NULL,
    address            varchar(400),
    location           geography(Point, 4326)  NOT NULL,     -- centro del sitio (ADR-009)
    timezone           varchar(64),                          -- override de tz del tenant
    -- overrides opcionales de política (NULL = hereda de company_settings)
    gps_accuracy_max_m integer  CHECK (gps_accuracy_max_m IS NULL OR gps_accuracy_max_m > 0),
    require_photo      boolean,
    require_biometric  boolean,
    status             varchar(20)             NOT NULL DEFAULT 'ACTIVE'
                       CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at         timestamptz             NOT NULL DEFAULT now(),
    updated_at         timestamptz             NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_work_sites_tenant_code ON work_sites (tenant_id, code);
CREATE INDEX        ix_work_sites_tenant       ON work_sites (tenant_id, status);
CREATE INDEX        gix_work_sites_location    ON work_sites USING gist (location);  -- índice espacial

CREATE TRIGGER trg_work_sites_updated_at BEFORE UPDATE ON work_sites
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- FK diferida de user_work_site_scope -> work_sites (definida como tabla en V2)
ALTER TABLE user_work_site_scope
    ADD CONSTRAINT fk_uwss_work_site FOREIGN KEY (work_site_id)
    REFERENCES work_sites (id) ON DELETE CASCADE;

-- Proyectos
CREATE TABLE projects (
    id         uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  uuid         NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    code       varchar(40)  NOT NULL,
    name       varchar(200) NOT NULL,
    status     varchar(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','CLOSED')),
    starts_on  date,
    ends_on    date,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_projects_tenant_code ON projects (tenant_id, code);

CREATE TRIGGER trg_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Relación proyecto <-> centro (N:M)
CREATE TABLE project_work_sites (
    project_id   uuid NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    work_site_id uuid NOT NULL REFERENCES work_sites (id) ON DELETE CASCADE,
    tenant_id    uuid NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, work_site_id)
);
