import { Injectable, inject } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import { environment } from '../../../environments/environment';
import { AuthStore } from '../../core/auth/auth.store';

export interface AttendanceEvent {
  type: 'ACCEPTED' | 'REJECTED';
  attendanceId: string;
  userId: string;
  workSiteId?: string;
  eventKind?: string;
  occurredAt: string;
  reason?: string;
}

/**
 * Cliente STOMP sobre SockJS para el tiempo real (ADR-011). Se suscribe al destino por
 * tenant `/topic/tenant/{tenantId}/attendance` y entrega los eventos de asistencia (RF-25).
 */
@Injectable({ providedIn: 'root' })
export class RealtimeService {
  private readonly store = inject(AuthStore);
  private client?: Client;

  connect(onEvent: (event: AttendanceEvent) => void, onStatus?: (connected: boolean) => void): void {
    const tenantId = this.store.user()?.tenantId;
    if (!tenantId) {
      return;
    }
    const token = this.store.accessToken();
    const url = window.location.origin + environment.wsUrl;

    this.client = new Client({
      webSocketFactory: () => new SockJS(url) as WebSocket,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      onConnect: () => {
        onStatus?.(true);
        this.client?.subscribe(`/topic/tenant/${tenantId}/attendance`, (message: IMessage) => {
          try {
            onEvent(JSON.parse(message.body) as AttendanceEvent);
          } catch {
            /* payload no-JSON: ignorar */
          }
        });
      },
      onWebSocketClose: () => onStatus?.(false),
    });
    this.client.activate();
  }

  disconnect(): void {
    void this.client?.deactivate();
    this.client = undefined;
  }
}
