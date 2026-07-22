import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface AttendanceReportFilters {
  from?: string; // ISO-8601 instant (p. ej. 2024-01-01T00:00:00Z)
  to?: string;
  status?: string;
  format: 'csv' | 'xlsx' | 'pdf';
}

/** Cliente REST de exportación de reportes de asistencia (RF-11). Requiere {@code report:export}. */
@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/reports`;

  attendance(filters: AttendanceReportFilters): Observable<HttpResponse<Blob>> {
    let params = new HttpParams().set('format', filters.format);
    if (filters.from) {
      params = params.set('from', filters.from);
    }
    if (filters.to) {
      params = params.set('to', filters.to);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    return this.http.get(`${this.base}/attendance`, { params, responseType: 'blob', observe: 'response' });
  }
}
