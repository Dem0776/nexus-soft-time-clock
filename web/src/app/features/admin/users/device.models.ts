/** Estado de reconocimiento de un dispositivo vinculado (RF-28, RN-27). */
export type DeviceStatus = 'PENDING' | 'TRUSTED' | 'BLOCKED';

/** Dispositivo vinculado a un colaborador. */
export interface Device {
  id: string;
  userId: string;
  deviceIdentifier: string;
  platform: string;
  model?: string;
  osVersion?: string;
  status: DeviceStatus;
  trusted: boolean;
  lastSeenAt?: string;
  createdAt?: string;
}

export const DEVICE_STATUS_LABELS: Record<DeviceStatus, string> = {
  TRUSTED: 'Confiado',
  PENDING: 'Pendiente de aprobación',
  BLOCKED: 'Bloqueado',
};
