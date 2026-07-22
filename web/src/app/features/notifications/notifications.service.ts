import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../../core/models/common.models';
import { AppNotification } from './notification.models';

/** Cliente REST de notificaciones del usuario autenticado (RF-27). */
@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/notifications`;

  mine(page = 0, size = 20): Observable<PageResponse<AppNotification>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AppNotification>>(`${this.base}/me`, { params });
  }
}
