import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Device } from './device.models';

/** Cliente REST de administración de dispositivos vinculados (RF-28, RN-27). Requiere {@code device:manage}. */
@Injectable({ providedIn: 'root' })
export class DeviceService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  /** Dispositivos de un colaborador (más reciente primero). */
  listByUser(userId: string): Observable<Device[]> {
    return this.http.get<Device[]>(`${this.base}/users/${userId}/devices`);
  }

  /** Confía un dispositivo (aprobación). */
  approve(deviceId: string): Observable<Device> {
    return this.http.post<Device>(`${this.base}/devices/${deviceId}/approve`, {});
  }

  /** Revoca/bloquea un dispositivo. */
  revoke(deviceId: string): Observable<Device> {
    return this.http.post<Device>(`${this.base}/devices/${deviceId}/revoke`, {});
  }
}
