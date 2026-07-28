import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { EmployeeProfile } from './employee-profile.models';

/**
 * Cliente REST del perfil de empleado (datos personales opcionales). Sub‑recurso de un
 * usuario: {@code /users/{id}/profile}. Requiere {@code user:manage}.
 */
@Injectable({ providedIn: 'root' })
export class EmployeeProfileService {
  private readonly http = inject(HttpClient);
  private base(userId: string): string {
    return `${environment.apiBaseUrl}/users/${userId}/profile`;
  }

  get(userId: string): Observable<EmployeeProfile> {
    return this.http.get<EmployeeProfile>(this.base(userId));
  }

  save(userId: string, profile: EmployeeProfile): Observable<EmployeeProfile> {
    return this.http.put<EmployeeProfile>(this.base(userId), profile);
  }
}
