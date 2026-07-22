-- =====================================================================
-- V1 — Extensiones y Tenancy (BC-02)
-- Nexus Soft Time Clock — PostgreSQL + PostGIS
-- Convenciones: snake_case, tablas en plural, UTC (timestamptz), tenant_id
--               como prefijo de índices de negocio (ADR-002).
-- =====================================================================

-- --- Extensiones -----------------------------------------------------
CREATE EXTENSION IF NOT EXISTS postgis;      -- tipos y funciones geoespaciales (ADR-009)
CREATE EXTENSION IF NOT EXISTS pgcrypto;     -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;       -- comparación case-insensitive (emails/dominios)
CREATE EXTENSION IF NOT EXISTS btree_gist;   -- índices GIST combinados

-- --- Función de auditoría de columna updated_at ----------------------
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_set_updated_at() IS 'Setea updated_at = now() en cada UPDATE (trigger BEFORE UPDATE).';

-- =====================================================================
-- companies (Tenant) — raíz del aislamiento multi-tenant
-- =====================================================================
CREATE TABLE companies (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    code          varchar(40)  NOT NULL,
    name          varchar(200) NOT NULL,
    legal_name    varchar(255),
    email_domain  citext,                              -- resolución de tenant por dominio (D-03)
    timezone      varchar(64)  NOT NULL DEFAULT 'UTC',  -- IANA tz del tenant (RNF-19)
    locale        varchar(10)  NOT NULL DEFAULT 'es',
    status        varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                  CONSTRAINT ck_companies_status CHECK (status IN ('ACTIVE','SUSPENDED','INACTIVE')),
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_companies_code CHECK (char_length(code) > 0)
);

CREATE UNIQUE INDEX uq_companies_code_idx        ON companies (lower(code));
CREATE UNIQUE INDEX uq_companies_email_domain_idx ON companies (email_domain) WHERE email_domain IS NOT NULL;

CREATE TRIGGER trg_companies_updated_at
    BEFORE UPDATE ON companies
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE companies IS 'Empresa / Tenant. Unidad de aislamiento multi-tenant (ADR-002).';

-- =====================================================================
-- company_settings — configuración y políticas por defecto del tenant
-- =====================================================================
CREATE TABLE company_settings (
    company_id                 uuid        PRIMARY KEY REFERENCES companies (id) ON DELETE CASCADE,
    default_gps_accuracy_max_m integer     NOT NULL DEFAULT 50   CHECK (default_gps_accuracy_max_m > 0),
    default_late_tolerance_min integer     NOT NULL DEFAULT 10   CHECK (default_late_tolerance_min >= 0),
    require_photo              boolean     NOT NULL DEFAULT false,
    require_biometric          boolean     NOT NULL DEFAULT false,
    mock_location_policy       varchar(10) NOT NULL DEFAULT 'REJECT' CHECK (mock_location_policy IN ('REJECT','FLAG')),
    rooted_device_policy       varchar(10) NOT NULL DEFAULT 'FLAG'   CHECK (rooted_device_policy IN ('REJECT','FLAG')),
    gps_spoof_policy           varchar(10) NOT NULL DEFAULT 'REJECT' CHECK (gps_spoof_policy IN ('REJECT','FLAG')),
    qr_ttl_seconds             integer     NOT NULL DEFAULT 120  CHECK (qr_ttl_seconds > 0),
    access_token_ttl_seconds   integer     NOT NULL DEFAULT 900,   -- 15 min (ADR-007)
    refresh_token_ttl_days     integer     NOT NULL DEFAULT 30,
    max_failed_logins          integer     NOT NULL DEFAULT 5,     -- RN-40
    settings_json              jsonb       NOT NULL DEFAULT '{}'::jsonb,
    created_at                 timestamptz NOT NULL DEFAULT now(),
    updated_at                 timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_company_settings_updated_at
    BEFORE UPDATE ON company_settings
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE company_settings IS 'Parámetros por defecto del tenant (precisión GPS, tolerancias, políticas antifraude, TTLs).';
