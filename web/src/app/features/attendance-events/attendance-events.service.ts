import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AttendanceEvent } from './attendance-events.models';

/** Cliente REST del reporte de registros de asistencia (entradas/salidas). Requiere {@code report:export}. */
@Injectable({ providedIn: 'root' })
export class AttendanceEventsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/reports/attendance-events`;
  private readonly evidenceBase = `${environment.apiBaseUrl}/attendance/evidence`;

  list(from: string, to: string): Observable<AttendanceEvent[]> {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<AttendanceEvent[]>(this.base, { params });
  }

  /**
   * URL firmada para ver la evidencia de un registro. Es de vida corta, así que se pide al abrir
   * el detalle y no se guarda. `serverTime` acota la búsqueda a una partición de la tabla.
   */
  evidenceUrl(recordId: string, serverTime: string): Observable<{ url: string }> {
    const params = new HttpParams().set('serverTime', serverTime);
    return this.http.get<{ url: string }>(`${this.evidenceBase}/${recordId}/url`, { params });
  }
}
