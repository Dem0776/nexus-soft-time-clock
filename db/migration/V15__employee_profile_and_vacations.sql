-- =====================================================================
-- V15 — Perfil de empleado y Vacaciones
-- Nexus Soft Time Clock — PostgreSQL
--
-- Añade:
--   1. employee_profiles   — datos personales OPCIONALES del colaborador
--                            (1‑a‑1 con users; no se toca la tabla users).
--   2. vacation_policies    — configuración de vacaciones por empresa
--                            (días/año, aprobación, conteo de días hábiles).
--   3. vacation_requests    — solicitudes de vacaciones y su resolución.
--   4. Permisos RBAC de vacaciones + asignación a roles plantilla y a los
--      roles ya instanciados de las empresas existentes.
--
-- Convenciones (V1): snake_case, plural, timestamptz UTC, tenant_id como
-- prefijo de índices, trigger fn_set_updated_at() para updated_at.
-- Nota: el perfil HR es autocontenido (incluye phone) para no acoplarse al
-- módulo identity/users; users.phone queda intacto.
-- =====================================================================

-- =====================================================================
-- 1) employee_profiles — información personal opcional del colaborador
-- =====================================================================
CREATE TABLE employee_profiles (
    user_id                 uuid        PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    tenant_id               uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    birth_date              date,                                   -- cumpleaños del colaborador
    hire_date               date,                                   -- ingreso a la empresa (antigüedad → vacaciones)
    gender                  varchar(20) CHECK (gender IS NULL OR gender IN
                            ('FEMALE','MALE','OTHER','UNDISCLOSED')),
    phone                   varchar(40),
    address                 varchar(400),
    emergency_contact_name  varchar(160),
    emergency_contact_phone varchar(40),
    created_at              timestamptz NOT NULL DEFAULT now(),
    updated_at              timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_employee_profiles_tenant     ON employee_profiles (tenant_id);
CREATE INDEX ix_employee_profiles_hire_date  ON employee_profiles (tenant_id, hire_date);
CREATE INDEX ix_employee_profiles_birth_mmdd ON employee_profiles
    (tenant_id, (extract(month from birth_date)), (extract(day from birth_date)))
    WHERE birth_date IS NOT NULL;   -- consultas de cumpleaños del mes

CREATE TRIGGER trg_employee_profiles_updated_at BEFORE UPDATE ON employee_profiles
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE employee_profiles IS
    'Datos personales opcionales del colaborador (1‑a‑1 con users). El teléfono vive en users.phone.';

-- =====================================================================
-- 2) vacation_policies — configuración de vacaciones por empresa (tenant)
-- =====================================================================
CREATE TABLE vacation_policies (
    tenant_id                uuid        PRIMARY KEY REFERENCES companies (id) ON DELETE CASCADE,
    days_per_year            integer     NOT NULL DEFAULT 12
                             CHECK (days_per_year >= 0 AND days_per_year <= 366),
    require_approval         boolean     NOT NULL DEFAULT true,   -- false = auto‑aprobación
    count_business_days_only boolean     NOT NULL DEFAULT true,   -- excluye sábados/domingos del conteo
    created_at               timestamptz NOT NULL DEFAULT now(),
    updated_at               timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_vacation_policies_updated_at BEFORE UPDATE ON vacation_policies
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE vacation_policies IS
    'Política de vacaciones por empresa: días base por año trabajado, aprobación y conteo de días hábiles.';

-- Semilla: cada empresa existente arranca con la política por defecto (12 días/año).
INSERT INTO vacation_policies (tenant_id)
SELECT id FROM companies
ON CONFLICT (tenant_id) DO NOTHING;

-- =====================================================================
-- 3) vacation_requests — solicitudes de vacaciones y su resolución
-- =====================================================================
CREATE TABLE vacation_requests (
    id               uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        uuid        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id          uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,   -- solicitante
    start_date       date        NOT NULL,
    end_date         date        NOT NULL,
    days             integer     NOT NULL CHECK (days > 0),   -- días solicitados según la política al momento
    reason           varchar(500),
    status           varchar(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED')),
    resolution_note  varchar(1000),
    resolved_by      uuid        REFERENCES users (id) ON DELETE SET NULL,
    resolved_at      timestamptz,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_vacation_requests_range CHECK (end_date >= start_date),
    -- Un colaborador no puede tener dos solicitudes vigentes (pendiente/aprobada)
    -- que se traslapen en fechas. Usa btree_gist (habilitado en V1).
    CONSTRAINT ex_vacation_requests_no_overlap
        EXCLUDE USING gist (
            user_id WITH =,
            daterange(start_date, end_date, '[]') WITH &&
        ) WHERE (status IN ('PENDING','APPROVED'))
);

CREATE INDEX ix_vacation_requests_tenant_status ON vacation_requests (tenant_id, status, start_date);
CREATE INDEX ix_vacation_requests_tenant_user   ON vacation_requests (tenant_id, user_id, start_date DESC);

CREATE TRIGGER trg_vacation_requests_updated_at BEFORE UPDATE ON vacation_requests
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE vacation_requests IS
    'Solicitudes de vacaciones del colaborador. La bandeja del panel (vacation:approve) las aprueba o rechaza.';

-- =====================================================================
-- 4) RBAC — permisos de vacaciones
-- =====================================================================
INSERT INTO permissions (code, resource, action, description) VALUES
    ('vacation:request', 'vacation', 'request', 'Solicitar vacaciones propias'),
    ('vacation:approve', 'vacation', 'approve', 'Ver y aprobar/rechazar solicitudes de vacaciones'),
    ('vacation:manage',  'vacation', 'manage',  'Configurar la política de vacaciones de la empresa')
ON CONFLICT (code) DO NOTHING;

-- --- Asignación a ROLES PLANTILLA del sistema (tenant_id = NULL) ------
-- SUPER_ADMIN ya recibe todos los permisos por el CROSS JOIN de V10; para
-- los nuevos permisos lo re‑aplicamos de forma idempotente.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code IN ('vacation:request','vacation:approve','vacation:manage')
WHERE r.tenant_id IS NULL AND r.code IN ('SUPER_ADMIN','COMPANY_ADMIN','HR_ADMIN')
ON CONFLICT DO NOTHING;

-- SUPERVISOR: puede ver/aprobar/rechazar solicitudes (no configura la política).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code IN ('vacation:approve')
WHERE r.tenant_id IS NULL AND r.code = 'SUPERVISOR'
ON CONFLICT DO NOTHING;

-- EMPLOYEE: puede solicitar vacaciones propias.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'vacation:request'
WHERE r.tenant_id IS NULL AND r.code = 'EMPLOYEE'
ON CONFLICT DO NOTHING;

-- --- Asignación a los ROLES YA INSTANCIADOS de empresas existentes ----
-- Los roles de cada tenant se instancian por copia al crear la empresa; para
-- que los tenants existentes reciban los nuevos permisos, replicamos la
-- matriz también sobre sus roles (emparejados por code).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code IN ('vacation:request','vacation:approve','vacation:manage')
WHERE r.tenant_id IS NOT NULL AND r.code IN ('SUPER_ADMIN','COMPANY_ADMIN','HR_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'vacation:approve'
WHERE r.tenant_id IS NOT NULL AND r.code = 'SUPERVISOR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'vacation:request'
WHERE r.tenant_id IS NOT NULL AND r.code = 'EMPLOYEE'
ON CONFLICT DO NOTHING;
