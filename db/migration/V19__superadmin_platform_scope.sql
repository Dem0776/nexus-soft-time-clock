-- =====================================================================
-- V19 — SUPER_ADMIN como administrador de PLATAFORMA
-- El rol plantilla SUPER_ADMIN (tenant_id NULL) queda limitado a la
-- gestión de empresas (company:manage). El resto de la operación de cada
-- tenant la administra su COMPANY_ADMIN. Corrige la asimetría por la que
-- SUPER_ADMIN (sin tenant) veía todo el menú pero los endpoints
-- tenant-scoped fallaban al exigir TenantContext.require().
-- V10 no se edita (ya aplicada); esta migración recorta sobre ella.
-- =====================================================================

DELETE FROM role_permissions rp
USING roles r, permissions p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.tenant_id IS NULL
  AND r.code = 'SUPER_ADMIN'
  AND p.code <> 'company:manage';
