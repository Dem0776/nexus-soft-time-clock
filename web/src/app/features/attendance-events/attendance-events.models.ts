/** Un registro individual de asistencia (espejo de AttendanceEventDto). */
export interface AttendanceEvent {
  serverTime: string; // ISO datetime
  userId: string;
  employeeName?: string;
  employeeCode?: string;
  workSite?: string;
  eventType: string;
  status: string;
}

/** Etiquetas en español de los tipos de evento. */
export const EVENT_LABELS: Record<string, string> = {
  ENTRADA: 'Hora entrada',
  SALIDA: 'Hora salida',
  INICIO_DESCANSO: 'Salida a comida',
  FIN_DESCANSO: 'Regreso de comida',
  CAMBIO_SITIO: 'Cambio de sitio',
};

export const EVENT_OPTIONS = Object.entries(EVENT_LABELS).map(([code, label]) => ({ code, label }));

export const STATUS_LABELS: Record<string, string> = {
  ACCEPTED: 'Aceptado',
  REJECTED: 'Rechazado',
  PENDING_REVIEW: 'En revisión',
};
