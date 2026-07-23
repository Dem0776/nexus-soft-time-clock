import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { EventTypeSetting } from './event-type.models';

/** Cliente REST del catálogo de tipos de evento por empresa (HU-12 CA1). */
@Injectable({ providedIn: 'root' })
export class EventTypesService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  list(): Observable<EventTypeSetting[]> {
    return this.http.get<EventTypeSetting[]>(`${this.base}/attendance/event-types`);
  }

  update(settings: EventTypeSetting[]): Observable<EventTypeSetting[]> {
    return this.http.put<EventTypeSetting[]>(`${this.base}/attendance/event-types`, settings);
  }
}
