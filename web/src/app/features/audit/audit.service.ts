import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../../core/models/common.models';
import { AuditEntry } from './audit.models';

/** Cliente REST de consulta de auditoría (RF-12). Solo lectura. Requiere {@code audit:read}. */
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/audit`;

  list(page = 0, size = 50): Observable<PageResponse<AuditEntry>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AuditEntry>>(this.base, { params });
  }
}
