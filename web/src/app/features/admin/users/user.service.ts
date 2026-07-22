import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { PageResponse } from '../../../core/models/common.models';
import { CreateUser, User, UserStatus } from './user.models';

/** Cliente REST de administración de usuarios del tenant (RF-06, RF-22). Requiere {@code user:manage}. */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/users`;

  list(page = 0, size = 20, search?: string): Observable<PageResponse<User>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<User>>(this.base, { params });
  }

  get(id: string): Observable<User> {
    return this.http.get<User>(`${this.base}/${id}`);
  }

  create(request: CreateUser): Observable<User> {
    return this.http.post<User>(this.base, request);
  }

  updateStatus(id: string, status: UserStatus): Observable<User> {
    return this.http.patch<User>(`${this.base}/${id}/status`, { status });
  }

  assignRoles(id: string, roleCodes: string[]): Observable<User> {
    return this.http.put<User>(`${this.base}/${id}/roles`, { roleCodes });
  }
}
