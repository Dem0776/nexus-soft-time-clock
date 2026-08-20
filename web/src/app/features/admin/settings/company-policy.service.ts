import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { CompanyPolicy } from './company-policy.models';

/** Cliente REST de las políticas de registro del tenant. Requiere {@code company:settings}. */
@Injectable({ providedIn: 'root' })
export class CompanyPolicyService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/company/settings`;

  get(): Observable<CompanyPolicy> {
    return this.http.get<CompanyPolicy>(this.base);
  }

  update(policy: CompanyPolicy): Observable<CompanyPolicy> {
    return this.http.put<CompanyPolicy>(this.base, policy);
  }
}
