import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { PageResponse } from '../../../core/models/common.models';
import { CreateProject, Project, UpdateProject } from './project.models';

/** Cliente REST de administración de proyectos (RF-23). Requiere {@code project:manage}. */
@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/projects`;

  list(page = 0, size = 20, search?: string): Observable<PageResponse<Project>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<Project>>(this.base, { params });
  }

  get(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.base}/${id}`);
  }

  create(request: CreateProject): Observable<Project> {
    return this.http.post<Project>(this.base, request);
  }

  update(id: string, request: UpdateProject): Observable<Project> {
    return this.http.put<Project>(`${this.base}/${id}`, request);
  }
}
