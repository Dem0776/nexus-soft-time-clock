import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AttendanceReport } from './report.models';

/**
 * Cliente REST del reporte agregado de asistencia (RF-11). Requiere {@code report:export}.
 * Devuelve el conjunto por colaborador; el filtrado, ordenamiento y exportación son client-side.
 */
@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/reports`;

  /** Reporte agregado por colaborador en el rango [from, to] (fechas yyyy-MM-dd). */
  summary(from?: string, to?: string): Observable<AttendanceReport[]> {
    let params = new HttpParams();
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }
    return this.http.get<AttendanceReport[]>(`${this.base}/attendance-summary`, { params });
  }
}
