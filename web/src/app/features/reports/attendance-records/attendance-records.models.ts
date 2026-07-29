import { AttendanceEventType } from '../../admin/event-types/event-type.models';

/** Estado de un registro de asistencia (coincide con el enum del backend). */
export type AttendanceRecordStatus = 'ACCEPTED' | 'REJECTED' | 'PENDING_REVIEW';

/**
 * Fila del reporte de registros individuales de asistencia (una por marcación). No expone
 * ids/llaves: el empleado se identifica por número + nombre y el centro por su nombre; el tipo
 * de evento y el estado son códigos de enum que el front traduce a etiquetas.
 */
export interface AttendanceRecordReport {
  employeeNumber: string;
  employeeName: string;
  /** Fecha y hora de servidor (ISO-8601). */
  serverTime: string;
  eventType: AttendanceEventType;
  status: AttendanceRecordStatus;
  rejectionReason?: string | null;
  workCenter: string;
}

export type RecordSortColumn = keyof Pick<
  AttendanceRecordReport,
  'employeeNumber' | 'employeeName' | 'serverTime' | 'eventType' | 'status' | 'workCenter'
>;

export interface RecordSortState {
  column: RecordSortColumn;
  direction: 'asc' | 'desc';
}

/** Etiquetas por defecto del estado; el color se resuelve aparte. */
export const STATUS_LABELS: Record<AttendanceRecordStatus, string> = {
  ACCEPTED: 'Aceptado',
  REJECTED: 'Rechazado',
  PENDING_REVIEW: 'En revisión',
};

/** Tono visual (clase .status-chip) por estado. */
export const STATUS_TONES: Record<AttendanceRecordStatus, string> = {
  ACCEPTED: 'success',
  REJECTED: 'danger',
  PENDING_REVIEW: 'warning',
};

/** Etiquetas de respaldo del tipo de evento cuando no hay configuración de empresa. */
export const DEFAULT_EVENT_TYPE_LABELS: Record<AttendanceEventType, string> = {
  ENTRADA: 'Entrada',
  SALIDA: 'Salida',
  INICIO_DESCANSO: 'Inicio de descanso',
  FIN_DESCANSO: 'Fin de descanso',
  CAMBIO_SITIO: 'Cambio de sitio',
};

/** Motivos de rechazo legibles (para el tooltip del estado). */
export const REJECTION_LABELS: Record<string, string> = {
  INVALID_QR: 'QR inválido',
  OUT_OF_GEOFENCE: 'Fuera de la geocerca',
  LOW_GPS_ACCURACY: 'Precisión de GPS baja',
  GPS_UNAVAILABLE: 'GPS no disponible',
  OUT_OF_SCHEDULE: 'Fuera de horario',
  FRAUD_MOCK_LOCATION: 'Ubicación simulada',
  FRAUD_ROOTED_DEVICE: 'Dispositivo comprometido',
  FRAUD_GPS_SPOOF_APP: 'App de suplantación de GPS',
  REPLAY_DETECTED: 'Registro duplicado',
  INVALID_SEQUENCE: 'Secuencia inválida',
  UNTRUSTED_DEVICE: 'Dispositivo no confiable',
  PHOTO_REQUIRED: 'Falta evidencia fotográfica',
  BIOMETRIC_REQUIRED: 'Falta verificación biométrica',
  EVENT_TYPE_DISABLED: 'Tipo de evento deshabilitado',
};
