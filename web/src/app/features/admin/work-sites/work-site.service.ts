import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { PageResponse } from '../../../core/models/common.models';
import { CreateWorkSite, UpdateWorkSite, WorkSite, WorkSiteStatus } from './work-site.models';

/** Cliente REST de administración de centros de trabajo (RF-07). Requiere {@code worksite:manage}. */
@Injectable({ providedIn: 'root' })
export class WorkSiteService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/work-sites`;

  list(page = 0, size = 20, search?: string): Observable<PageResponse<WorkSite>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<WorkSite>>(this.base, { params });
  }

  get(id: string): Observable<WorkSite> {
    return this.http.get<WorkSite>(`${this.base}/${id}`);
  }

  create(request: CreateWorkSite): Observable<WorkSite> {
    return this.http.post<WorkSite>(this.base, request);
  }

  update(id: string, request: UpdateWorkSite): Observable<WorkSite> {
    return this.http.put<WorkSite>(`${this.base}/${id}`, request);
  }

  setStatus(id: string, status: WorkSiteStatus): Observable<WorkSite> {
    return this.http.patch<WorkSite>(`${this.base}/${id}/status`, { status });
  }
}
