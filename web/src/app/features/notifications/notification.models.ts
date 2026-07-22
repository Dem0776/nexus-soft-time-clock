/** Notificación del usuario autenticado (RF-27). */
export interface AppNotification {
  id: string;
  channel: string;
  type: string;
  title: string;
  body: string;
  status: string;
}
