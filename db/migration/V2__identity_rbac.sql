-- =====================================================================
-- V2 — Identity & Access + RBAC (BC-01)
-- Usuarios, roles, permisos, refresh tokens, dispositivos, scoping.
-- =====================================================================

-- =====================================================================
-- users
-- =====================================================================
CREATE TABLE users (
    id                 uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          uuid         REFERENCES companies (id) ON DELETE CASCADE,  -- NULL solo para plataforma
    is_platform_admin  boolean      NOT NULL DEFAULT false,
    email              citext       NOT NULL,
    password_hash      varchar(255) NOT NULL,           -- argon2/bcrypt
    first_name         varchar(120) NOT NULL,
    last_name          varchar(120) NOT NULL,
    employee_code      varchar(60),
    phone              varchar(40),
    status             varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                       CHECK (status IN ('ACTIVE','INACTIVE','LOCKED','INVITED')),
    failed_login_count integer      NOT NULL DEFAULT 0,
    locked_until       timestamptz,
    last_login_at      timestamptz,
    created_at         timestamptz  NOT NULL DEFAULT now(),
    updated_at         timestamptz  NOT NULL DEFAULT now(),
    -- RN-32: usuario pertenece a un tenant, salvo super admin de plataforma
    CONSTRAINT ck_users_tenant_scope CHECK (
        (is_platform_admin AND tenant_id IS NULL) OR
        (NOT is_platform_admin AND tenant_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_users_tenant_email     ON users (tenant_id, email) WHERE tenant_id IS NOT NULL;
CREATE UNIQUE INDEX uq_users_platform_email   ON users (email) WHERE tenant_id IS NULL;
CREATE UNIQUE INDEX uq_users_tenant_empcode   ON users (tenant_id, employee_code) WHERE employee_code IS NOT NULL;
CREATE INDEX        ix_users_tenant_status    ON users (tenant_id, status);

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

COMMENT ON TABLE users IS 'Cuentas de acceso. tenant_id NULL + is_platform_admin => SUPER_ADMIN de plataforma.';

-- =====================================================================
-- permissions — catálogo global de permisos granulares (recurso:acción)
-- =====================================================================
CREATE TABLE permissions (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    code        varchar(80) NOT NULL UNIQUE,   -- p.ej. 'attendance:register'
    resource    varchar(40) NOT NULL,
    action      varchar(40) NOT NULL,
    description varchar(200)
);

COMMENT ON TABLE permissions IS 'Catálogo global de permisos RBAC (BC-01).';

-- =====================================================================
-- roles — por tenant; tenant_id NULL = rol plantilla del sistema
-- =====================================================================
CREATE TABLE roles (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   uuid        REFERENCES companies (id) ON DELETE CASCADE,
    code        varchar(40) NOT NULL,          -- SUPER_ADMIN, COMPANY_ADMIN, HR_ADMIN, SUPERVISOR, AUDITOR, EMPLOYEE
    name        varchar(120) NOT NULL,
    is_system   boolean     NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_roles_tenant_code    ON roles (tenant_id, code) WHERE tenant_id IS NOT NULL;
CREATE UNIQUE INDEX uq_roles_system_code    ON roles (code) WHERE tenant_id IS NULL;

CREATE TRIGGER trg_roles_updated_at BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- role_permissions (N:M)
CREATE TABLE role_permissions (
    role_id       uuid NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- user_roles (N:M)
CREATE TABLE user_roles (
    user_id    uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id    uuid NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    granted_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

-- =====================================================================
-- devices — device binding (RF-28, RN-27)
-- =====================================================================
CREATE TABLE devices (
    id                uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         uuid         NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id           uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    device_identifier varchar(200) NOT NULL,          -- id estable del dispositivo
    platform          varchar(20)  NOT NULL CHECK (platform IN ('ANDROID','IOS','WEB')),
    model             varchar(120),
    os_version        varchar(60),
    push_token        varchar(255),                   -- FCM
    is_trusted        boolean      NOT NULL DEFAULT false,
    status            varchar(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','BLOCKED')),
    last_seen_at      timestamptz,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_devices_user_identifier ON devices (tenant_id, user_id, device_identifier);
CREATE INDEX        ix_devices_tenant_user     ON devices (tenant_id, user_id);

CREATE TRIGGER trg_devices_updated_at BEFORE UPDATE ON devices
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- =====================================================================
-- refresh_tokens — rotación y familias (ADR-007)
-- =====================================================================
CREATE TABLE refresh_tokens (
    id           uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid         REFERENCES companies (id) ON DELETE CASCADE,
    user_id      uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    device_id    uuid         REFERENCES devices (id) ON DELETE SET NULL,
    family_id    uuid         NOT NULL,                 -- misma familia = misma cadena de rotación
    token_hash   varchar(128) NOT NULL,                 -- hash del token, nunca el token en claro
    parent_id    uuid         REFERENCES refresh_tokens (id) ON DELETE SET NULL,
    replaced_by  uuid,
    expires_at   timestamptz  NOT NULL,
    revoked_at   timestamptz,
    created_ip   inet,
    user_agent   varchar(400),
    created_at   timestamptz  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_refresh_tokens_hash     ON refresh_tokens (token_hash);
CREATE INDEX        ix_refresh_tokens_user     ON refresh_tokens (user_id);
CREATE INDEX        ix_refresh_tokens_family   ON refresh_tokens (family_id);
CREATE INDEX        ix_refresh_tokens_expires  ON refresh_tokens (expires_at) WHERE revoked_at IS NULL;

COMMENT ON TABLE refresh_tokens IS 'Refresh tokens rotatorios; reutilización revoca la familia (RN-41).';

-- =====================================================================
-- user_work_site_scope — ámbito de supervisor por centro (RN-33)
-- =====================================================================
CREATE TABLE user_work_site_scope (
    user_id      uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    work_site_id uuid NOT NULL,                 -- FK añadida en V3 (organization)
    tenant_id    uuid NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, work_site_id)
);
