import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../../core/models/common.models';
import { Incident, ResolveIncident } from './incident.models';

/** Cliente REST de gestión de incidencias (RF-09). Requiere {@code incident:approve}. */
@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/incidents`;

  list(status?: string, page = 0, size = 20): Observable<PageResponse<Incident>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<Incident>>(this.base, { params });
  }

  resolve(id: string, request: ResolveIncident): Observable<Incident> {
    return this.http.patch<Incident>(`${this.base}/${id}/resolve`, request);
  }
}
