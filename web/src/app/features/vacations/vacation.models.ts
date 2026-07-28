/** Estados de una solicitud de vacaciones (espejo de VacationStatus del backend). */
export type VacationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

/** Solicitud de vacaciones (espejo de VacationRequestResponse). */
export interface VacationRequest {
  id: string;
  userId: string;
  userName?: string;
  employeeCode?: string;
  startDate: string; // ISO date (yyyy-MM-dd)
  endDate: string;
  days: number;
  reason?: string;
  status: VacationStatus;
  resolutionNote?: string;
  resolvedBy?: string;
  resolvedAt?: string;
  createdAt: string;
}

/** Resolución posible desde el panel (aprobar o rechazar). */
export type VacationResolution = 'APPROVED' | 'REJECTED';

/** Cuerpo del PATCH de resolución. */
export interface ResolveVacation {
  resolution: VacationResolution;
  note?: string;
}

/** Alta de una solicitud (para uso futuro desde el panel/app). */
export interface CreateVacationRequest {
  userId?: string; // opcional: si lo omite el backend, se asume el usuario autenticado
  startDate: string;
  endDate: string;
  reason?: string;
}
