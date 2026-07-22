import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { PageResponse } from '../../../core/models/common.models';
import {
  Assignment,
  AssignmentRequest,
  CreateSchedule,
  Schedule,
  Shift,
  ShiftRequest,
  UpdateSchedule,
} from './scheduling.models';

/** Cliente REST de horarios, turnos y asignaciones (RF-08). Requiere {@code schedule:manage}. */
@Injectable({ providedIn: 'root' })
export class SchedulingService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // --- Horarios ---
  listSchedules(page = 0, size = 20, search?: string): Observable<PageResponse<Schedule>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<Schedule>>(`${this.base}/schedules`, { params });
  }

  createSchedule(request: CreateSchedule): Observable<Schedule> {
    return this.http.post<Schedule>(`${this.base}/schedules`, request);
  }

  updateSchedule(id: string, request: UpdateSchedule): Observable<Schedule> {
    return this.http.put<Schedule>(`${this.base}/schedules/${id}`, request);
  }

  // --- Turnos ---
  listShifts(scheduleId: string): Observable<Shift[]> {
    return this.http.get<Shift[]>(`${this.base}/schedules/${scheduleId}/shifts`);
  }

  createShift(scheduleId: string, request: ShiftRequest): Observable<Shift> {
    return this.http.post<Shift>(`${this.base}/schedules/${scheduleId}/shifts`, request);
  }

  updateShift(shiftId: string, request: ShiftRequest): Observable<Shift> {
    return this.http.put<Shift>(`${this.base}/shifts/${shiftId}`, request);
  }

  // --- Asignaciones ---
  assign(request: AssignmentRequest): Observable<Assignment> {
    return this.http.post<Assignment>(`${this.base}/shift-assignments`, request);
  }

  listAssignments(userId: string): Observable<Assignment[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<Assignment[]>(`${this.base}/shift-assignments`, { params });
  }
}
