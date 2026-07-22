import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { PageResponse } from '../../../core/models/common.models';
import { Company, CompanyStatus, CreateCompany, UpdateCompany } from './company.models';

/** Cliente REST de administración de empresas (RF-13). */
@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/companies`;

  list(page = 0, size = 20, search?: string): Observable<PageResponse<Company>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<Company>>(this.base, { params });
  }

  get(id: string): Observable<Company> {
    return this.http.get<Company>(`${this.base}/${id}`);
  }

  create(request: CreateCompany): Observable<Company> {
    return this.http.post<Company>(this.base, request);
  }

  update(id: string, request: UpdateCompany): Observable<Company> {
    return this.http.put<Company>(`${this.base}/${id}`, request);
  }

  setStatus(id: string, status: CompanyStatus): Observable<Company> {
    return this.http.patch<Company>(`${this.base}/${id}/status`, { status });
  }
}
