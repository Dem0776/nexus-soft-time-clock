-- =====================================================================
-- V10 — Semilla de referencia: catálogo de permisos y roles plantilla
-- Datos NO ligados a un tenant (plantillas del sistema). Al crear una
-- empresa, la aplicación instancia sus roles a partir de estas plantillas.
-- =====================================================================

-- --- Permisos (catálogo global) --------------------------------------
INSERT INTO permissions (code, resource, action, description) VALUES
    ('company:manage',      'company',    'manage',   'Gestión de empresas (tenants)'),
    ('user:manage',         'user',       'manage',   'Gestión de usuarios'),
    ('role:manage',         'role',       'manage',   'Gestión de roles y permisos'),
    ('worksite:manage',     'worksite',   'manage',   'Gestión de centros de trabajo'),
    ('project:manage',      'project',    'manage',   'Gestión de proyectos'),
    ('schedule:manage',     'schedule',   'manage',   'Gestión de horarios y turnos'),
    ('geofence:manage',     'geofence',   'manage',   'Gestión de geocercas y QR'),
    ('attendance:read',     'attendance', 'read',     'Consulta de asistencias del ámbito'),
    ('attendance:register', 'attendance', 'register', 'Registrar asistencia propia'),
    ('incident:approve',    'incident',   'approve',  'Aprobar/rechazar incidencias'),
    ('report:export',       'report',     'export',   'Exportar reportes'),
    ('audit:read',          'audit',      'read',     'Consultar bitácora de auditoría'),
    ('dashboard:read',      'dashboard',  'read',     'Ver dashboards y mapa en tiempo real'),
    ('sync:read',           'sync',       'read',     'Ver estado de sincronización')
ON CONFLICT (code) DO NOTHING;

-- --- Roles plantilla del sistema (tenant_id = NULL) ------------------
INSERT INTO roles (id, tenant_id, code, name, is_system) VALUES
    (gen_random_uuid(), NULL, 'SUPER_ADMIN',   'Super Administrador', true),
    (gen_random_uuid(), NULL, 'COMPANY_ADMIN', 'Administrador de Empresa', true),
    (gen_random_uuid(), NULL, 'HR_ADMIN',      'Administrador RR.HH.', true),
    (gen_random_uuid(), NULL, 'SUPERVISOR',    'Supervisor', true),
    (gen_random_uuid(), NULL, 'AUDITOR',       'Auditor', true),
    (gen_random_uuid(), NULL, 'EMPLOYEE',      'Colaborador', true)
ON CONFLICT DO NOTHING;

-- --- Asignación de permisos a roles plantilla (matriz de autorización) ---
-- SUPER_ADMIN: todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.tenant_id IS NULL AND r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- COMPANY_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'user:manage','role:manage','worksite:manage','project:manage','schedule:manage',
    'geofence:manage','attendance:read','incident:approve','report:export','audit:read',
    'dashboard:read','sync:read')
WHERE r.tenant_id IS NULL AND r.code = 'COMPANY_ADMIN'
ON CONFLICT DO NOTHING;

-- HR_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'project:manage','schedule:manage','attendance:read','incident:approve',
    'report:export','dashboard:read')
WHERE r.tenant_id IS NULL AND r.code = 'HR_ADMIN'
ON CONFLICT DO NOTHING;

-- SUPERVISOR (ámbito por centro, aplicado en capa de aplicación con RN-33)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'attendance:read','incident:approve','report:export','dashboard:read','sync:read')
WHERE r.tenant_id IS NULL AND r.code = 'SUPERVISOR'
ON CONFLICT DO NOTHING;

-- AUDITOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('audit:read')
WHERE r.tenant_id IS NULL AND r.code = 'AUDITOR'
ON CONFLICT DO NOTHING;

-- EMPLOYEE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('attendance:register')
WHERE r.tenant_id IS NULL AND r.code = 'EMPLOYEE'
ON CONFLICT DO NOTHING;
