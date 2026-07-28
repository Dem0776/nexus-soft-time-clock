import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';

import { PageResponse } from '../../core/models/common.models';
import { EmployeeProfile } from '../admin/users/employee-profile.models';
import { EmployeeProfileService } from '../admin/users/employee-profile.service';
import { User } from '../admin/users/user.models';
import { UserService } from '../admin/users/user.service';
import { VacationRequest } from '../vacations/vacation.models';
import { VacationService } from '../vacations/vacation.service';
import { Person } from './people.models';

/**
 * Arma el reporte de Personas combinando el listado de usuarios, su perfil (datos personales)
 * y el conteo de solicitudes de vacaciones pendientes, y deriva indicadores (cumpleaños,
 * aniversario, antigüedad). v1: ensambla en el cliente; para tenants grandes conviene un
 * endpoint agregado en el backend (GET /people).
 */
@Injectable({ providedIn: 'root' })
export class PeopleService {
  private readonly users = inject(UserService);
  private readonly profiles = inject(EmployeeProfileService);
  private readonly vacations = inject(VacationService);

  list(): Observable<Person[]> {
    const emptyPage: PageResponse<VacationRequest> = { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0 };
    return forkJoin({
      users: this.users.list(0, 500),
      pending: this.vacations.list(0, 1000, 'PENDING').pipe(catchError(() => of(emptyPage))),
    }).pipe(
      switchMap(({ users, pending }) => {
        const pendingBy = new Map<string, number>();
        pending.content.forEach((v) => pendingBy.set(v.userId, (pendingBy.get(v.userId) ?? 0) + 1));
        const list = users.content;
        if (!list.length) {
          return of([] as Person[]);
        }
        return forkJoin(
          list.map((u) =>
            this.profiles.get(u.id).pipe(
              catchError(() => of({} as EmployeeProfile)),
              map((p) => this.assemble(u, p, pendingBy.get(u.id) ?? 0)),
            ),
          ),
        );
      }),
    );
  }

  private assemble(u: User, p: EmployeeProfile, pending: number): Person {
    const today = new Date();
    const birth = this.parse(p.birthDate);
    const hire = this.parse(p.hireDate);
    const years = hire ? this.completedYears(hire, today) : null;
    return {
      id: u.id,
      email: u.email,
      firstName: u.firstName,
      lastName: u.lastName,
      fullName: `${u.firstName} ${u.lastName}`.trim(),
      employeeCode: u.employeeCode,
      status: u.status,
      roles: u.roles ?? [],
      phone: p.phone,
      gender: p.gender ?? undefined,
      birthDate: p.birthDate,
      hireDate: p.hireDate,
      address: p.address,
      emergencyContactName: p.emergencyContactName,
      emergencyContactPhone: p.emergencyContactPhone,
      pendingVacations: pending,
      yearsOfService: years,
      isBirthdayToday: this.sameDayMonth(birth, today),
      isAnniversaryToday: this.sameDayMonth(hire, today) && (years ?? 0) >= 1,
    };
  }

  private parse(s?: string): Date | null {
    if (!s) return null;
    const d = new Date(`${s}T00:00:00`);
    return isNaN(d.getTime()) ? null : d;
  }

  private sameDayMonth(d: Date | null, today: Date): boolean {
    return !!d && d.getMonth() === today.getMonth() && d.getDate() === today.getDate();
  }

  private completedYears(from: Date, to: Date): number {
    let years = to.getFullYear() - from.getFullYear();
    const m = to.getMonth() - from.getMonth();
    if (m < 0 || (m === 0 && to.getDate() < from.getDate())) years--;
    return Math.max(0, years);
  }
}
