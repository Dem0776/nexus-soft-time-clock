import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { Geofence, GeofenceRequest, QrRequest, QrToken } from './geofence.models';

/** Geocercas y QR firmado por centro (RF-10, RF-14). Requiere {@code geofence:manage}. */
@Injectable({ providedIn: 'root' })
export class GeofenceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/work-sites`;

  get(workSiteId: string): Observable<Geofence> {
    return this.http.get<Geofence>(`${this.base}/${workSiteId}/geofence`);
  }

  upsert(workSiteId: string, request: GeofenceRequest): Observable<Geofence> {
    return this.http.put<Geofence>(`${this.base}/${workSiteId}/geofence`, request);
  }

  generateQr(workSiteId: string, request?: QrRequest): Observable<QrToken> {
    return this.http.post<QrToken>(`${this.base}/${workSiteId}/qr`, request ?? {});
  }
}
