import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { AttendanceRecordReport } from './attendance-records.models';

/**
 * Cliente REST del reporte de registros individuales de asistencia (RF-11). Requiere
 * {@code report:export}. Devuelve una fila por marcación; el filtrado, orden y paginación
 * finos se hacen en el cliente.
 */
@Injectable({ providedIn: 'root' })
export class AttendanceRecordsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/reports`;

  /** Registros en el rango [from, to] (fechas yyyy-MM-dd) y estado opcional. */
  records(from?: string, to?: string, status?: string): Observable<AttendanceRecordReport[]> {
    let params = new HttpParams();
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<AttendanceRecordReport[]>(`${this.base}/attendance-records`, { params });
  }
}
