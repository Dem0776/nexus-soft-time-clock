-- =====================================================================
-- V21 — Política de registro por empresa administrable (RF-18 / HU-13)
--
-- Las columnas ya existen: company_settings.require_photo/require_biometric
-- (V1) y work_sites.require_photo/require_biometric (V3, NULLABLE porque
-- NULL = "heredar de company_settings"). Lo que faltaba era (a) que el
-- backend leyera de verdad la política de empresa —hasta ahora el NULL del
-- centro se trataba como false y la dejaba sin efecto— y (b) un permiso
-- para editarla desde el portal, en lugar de por SQL.
--
-- Esta migración solo añade el permiso RBAC y documenta la herencia.
-- Patrón de RBAC replicado de V20 (plantilla + roles ya instanciados).
-- =====================================================================

-- =====================================================================
-- 1) RBAC — permiso de administración de la configuración de la empresa
-- =====================================================================
INSERT INTO permissions (code, resource, action, description) VALUES
    ('company:settings', 'company', 'settings', 'Editar las políticas por defecto de la empresa')
ON CONFLICT (code) DO NOTHING;

-- --- ROLES PLANTILLA del sistema (tenant_id = NULL) ------------------
-- No se asigna a SUPER_ADMIN: V19 lo limita a company:manage (plataforma).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'company:settings'
WHERE r.tenant_id IS NULL AND r.code IN ('COMPANY_ADMIN', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

-- --- ROLES YA INSTANCIADOS de empresas existentes -------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code = 'company:settings'
WHERE r.tenant_id IS NOT NULL AND r.code IN ('COMPANY_ADMIN', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- 2) Red de seguridad: toda empresa debe tener su fila de configuración
-- El alta de empresa ya la crea, pero un tenant antiguo sin fila caería
-- siempre en los defaults del código en vez de en su política real.
-- =====================================================================
INSERT INTO company_settings (company_id)
SELECT id FROM companies
ON CONFLICT (company_id) DO NOTHING;

-- =====================================================================
-- 3) Documentación de la herencia en el propio esquema
-- =====================================================================
COMMENT ON COLUMN company_settings.require_photo IS
    'Política por defecto del tenant: exigir evidencia fotográfica al registrar (HU-13 CA1). '
    'Se aplica a los centros cuyo work_sites.require_photo sea NULL.';
COMMENT ON COLUMN company_settings.require_biometric IS
    'Política por defecto del tenant: exigir verificación biométrica al registrar (HU-14 CA1). '
    'Se aplica a los centros cuyo work_sites.require_biometric sea NULL.';
COMMENT ON COLUMN work_sites.require_photo IS
    'Override del centro. NULL = heredar de company_settings.require_photo; TRUE/FALSE lo sobrescriben.';
COMMENT ON COLUMN work_sites.require_biometric IS
    'Override del centro. NULL = heredar de company_settings.require_biometric; TRUE/FALSE lo sobrescriben.';
