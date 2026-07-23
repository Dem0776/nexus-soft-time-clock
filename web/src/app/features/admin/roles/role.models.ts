/** Plantilla de rol del sistema (espejo de RoleController.RoleResponse). */
export interface Role {
  code: string;
  name: string;
}

/**
 * Nivel de privilegio por rol (espejo de RoleGrantPolicy en el backend). Un operador solo
 * puede otorgar roles de rango estrictamente inferior al suyo. La frontera real es el
 * servidor; este mapa solo alinea la UI. Rol desconocido → no otorgable (Infinity).
 */
export const ROLE_RANK: Record<string, number> = {
  SUPER_ADMIN: 100,
  COMPANY_ADMIN: 80,
  HR_ADMIN: 60,
  SUPERVISOR: 60,
  AUDITOR: 40,
  EMPLOYEE: 20,
};

export function rankOf(code: string): number {
  return ROLE_RANK[code] ?? Number.POSITIVE_INFINITY;
}
