import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { VacationPolicy } from './vacation-policy.models';

/** Cliente REST de la política de vacaciones del tenant. Requiere {@code vacation:manage}. */
@Injectable({ providedIn: 'root' })
export class VacationPolicyService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/vacations/policy`;

  get(): Observable<VacationPolicy> {
    return this.http.get<VacationPolicy>(this.base);
  }

  update(policy: VacationPolicy): Observable<VacationPolicy> {
    return this.http.put<VacationPolicy>(this.base, policy);
  }
}
