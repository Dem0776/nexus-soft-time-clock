import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Role } from './role.models';

/** Cliente REST del catálogo de roles (RF-22). Requiere {@code role:manage}. */
@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/roles`;

  list(): Observable<Role[]> {
    return this.http.get<Role[]>(this.base);
  }
}
