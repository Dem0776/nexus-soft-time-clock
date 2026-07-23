/** Tipos de evento de asistencia (deben coincidir con el enum del backend). */
export type AttendanceEventType =
  | 'ENTRADA'
  | 'SALIDA'
  | 'INICIO_DESCANSO'
  | 'FIN_DESCANSO'
  | 'CAMBIO_SITIO';

/** Configuración por empresa de un tipo de evento (HU-12 CA1). */
export interface EventTypeSetting {
  eventType: AttendanceEventType;
  enabled: boolean;
  label: string;
}

/** ENTRADA y SALIDA son núcleo: siempre habilitadas y no configurables. */
export const CORE_EVENT_TYPES: AttendanceEventType[] = ['ENTRADA', 'SALIDA'];

export function isCore(type: AttendanceEventType): boolean {
  return CORE_EVENT_TYPES.includes(type);
}
