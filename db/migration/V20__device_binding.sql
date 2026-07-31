-- =====================================================================
-- V20 — Device binding (RF-28, RN-27)
-- La tabla `devices` ya existe desde V2 (id, tenant_id, user_id,
-- device_identifier, platform, model, os_version, is_trusted, status…);
-- esta migración solo añade la POLÍTICA por tenant y el permiso RBAC que
-- gobierna su administración. El enrolamiento es TOFU: el primer
-- dispositivo de un colaborador se confía en su primer uso; los adicionales
-- quedan a la espera de aprobación (is_trusted = false).
--
-- Convenciones (V1): snake_case, política como varchar con CHECK, tenant_id
-- prefijo de índices. Patrón de RBAC replicado de V15 (plantilla + roles ya
-- instanciados de las empresas existentes).
-- =====================================================================

-- =====================================================================
-- 1) Política de device binding por empresa (tenant)
-- =====================================================================
ALTER TABLE company_settings
    ADD COLUMN device_binding_enabled boolean     NOT NULL DEFAULT true,
    ADD COLUMN device_binding_action  varchar(10) NOT NULL DEFAULT 'REJECT'
               CHECK (device_binding_action IN ('REJECT','FLAG'));

COMMENT ON COLUMN company_settings.device_binding_enabled IS
    'Si true (RN-27), se valida que la asistencia provenga de un dispositivo reconocido del colaborador.';
COMMENT ON COLUMN company_settings.device_binding_action IS
    'Acción ante un dispositivo NO reconocido: REJECT bloquea el registro (UNTRUSTED_DEVICE); FLAG solo lo marca.';

-- =====================================================================
-- 2) RBAC — permiso de administración de dispositivos
-- =====================================================================
INSERT INTO permissions (code, resource, action, description) VALUES
    ('device:manage', 'device', 'manage', 'Ver y aprobar/revocar dispositivos de los colaboradores')
ON CONFLICT (code) DO NOTHING;

-- --- ROLES PLANTILLA del sistema (tenant_id = NULL) ------------------
-- No se asigna a SUPER_ADMIN: V19 lo limita a company:manage (plataforma).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'device:manage'
WHERE r.tenant_id IS NULL AND r.code = 'COMPANY_ADMIN'
ON CONFLICT DO NOTHING;

-- --- ROLES YA INSTANCIADOS de empresas existentes -------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'device:manage'
WHERE r.tenant_id IS NOT NULL AND r.code = 'COMPANY_ADMIN'
ON CONFLICT DO NOTHING;
