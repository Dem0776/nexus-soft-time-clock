import { Component, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';

import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { User } from '../users/user.models';
import { UserService } from '../users/user.service';
import { WorkSite } from '../work-sites/work-site.models';
import { WorkSiteService } from '../work-sites/work-site.service';
import { SCHEDULE_STATUSES, Assignment, Schedule, Shift } from './scheduling.models';
import { SchedulingService } from './scheduling.service';

/** Administración de horarios, turnos y asignaciones (RF-08). Requiere {@code schedule:manage}. */
@Component({
  selector: 'app-scheduling',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    PageHeaderComponent,
    StatusChipComponent,
  ],
  template: `
    <app-page-header title="Horarios y turnos" />
    @if (error()) { <p class="error-text">{{ error() }}</p> }

    <mat-tab-group>
      <!-- Horarios y sus turnos -->
      <mat-tab label="Horarios y turnos">
        <div style="display:flex;gap:16px;flex-wrap:wrap;margin-top:16px">
          <mat-card style="flex:1 1 380px">
            <mat-card-header><mat-card-title>Horarios</mat-card-title></mat-card-header>
            <mat-card-content>
              <form [formGroup]="scheduleForm" (ngSubmit)="saveSchedule()" style="display:flex;gap:8px;flex-wrap:wrap;align-items:baseline">
                <mat-form-field appearance="outline" style="width:110px">
                  <mat-label>Código</mat-label>
                  <input matInput formControlName="code" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Nombre</mat-label>
                  <input matInput formControlName="name" />
                </mat-form-field>
                <mat-form-field appearance="outline" style="width:150px">
                  <mat-label>Zona horaria</mat-label>
                  <input matInput formControlName="timezone" placeholder="America/Lima" />
                </mat-form-field>
                @if (editingScheduleId()) {
                  <mat-form-field appearance="outline" style="width:130px">
                    <mat-label>Estado</mat-label>
                    <mat-select formControlName="status">
                      @for (s of scheduleStatuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
                    </mat-select>
                  </mat-form-field>
                }
                <button mat-flat-button color="primary" type="submit" [disabled]="scheduleForm.invalid">
                  {{ editingScheduleId() ? 'Guardar' : 'Crear' }}
                </button>
                @if (editingScheduleId()) {
                  <button mat-button type="button" (click)="resetScheduleForm()">Cancelar</button>
                }
              </form>

              <table mat-table [dataSource]="schedules()" style="width:100%;margin-top:12px">
                <ng-container matColumnDef="code">
                  <th mat-header-cell *matHeaderCellDef>Código</th>
                  <td mat-cell *matCellDef="let s">{{ s.code }}</td>
                </ng-container>
                <ng-container matColumnDef="name">
                  <th mat-header-cell *matHeaderCellDef>Nombre</th>
                  <td mat-cell *matCellDef="let s">{{ s.name }}</td>
                </ng-container>
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Estado</th>
                  <td mat-cell *matCellDef="let s"><app-status-chip [status]="s.status" /></td>
                </ng-container>
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef></th>
                  <td mat-cell *matCellDef="let s" style="text-align:right;white-space:nowrap">
                    <button mat-icon-button (click)="selectSchedule(s)" aria-label="Turnos"><mat-icon>list</mat-icon></button>
                    <button mat-icon-button (click)="editSchedule(s)" aria-label="Editar"><mat-icon>edit</mat-icon></button>
                  </td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="scheduleColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: scheduleColumns"
                    [style.background]="row.id === selectedSchedule()?.id ? 'rgba(21,101,192,.08)' : ''"></tr>
              </table>
            </mat-card-content>
          </mat-card>

          <mat-card style="flex:1 1 460px">
            <mat-card-header>
              <mat-card-title>
                Turnos {{ selectedSchedule() ? 'de ' + selectedSchedule()!.name : '' }}
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              @if (!selectedSchedule()) {
                <p class="muted">Seleccioná un horario para gestionar sus turnos.</p>
              } @else {
                <form [formGroup]="shiftForm" (ngSubmit)="saveShift()" style="display:flex;gap:8px;flex-wrap:wrap;align-items:baseline">
                  <mat-form-field appearance="outline" style="width:140px">
                    <mat-label>Nombre</mat-label>
                    <input matInput formControlName="name" />
                  </mat-form-field>
                  <mat-form-field appearance="outline" style="width:120px">
                    <mat-label>Entrada</mat-label>
                    <input matInput type="time" formControlName="startTime" />
                  </mat-form-field>
                  <mat-form-field appearance="outline" style="width:120px">
                    <mat-label>Salida</mat-label>
                    <input matInput type="time" formControlName="endTime" />
                  </mat-form-field>
                  <mat-form-field appearance="outline" style="width:130px">
                    <mat-label>Tolerancia (min)</mat-label>
                    <input matInput type="number" formControlName="lateToleranceMin" />
                  </mat-form-field>
                  <mat-checkbox formControlName="crossesMidnight">Cruza medianoche</mat-checkbox>
                  <button mat-flat-button color="primary" type="submit" [disabled]="shiftForm.invalid">
                    {{ editingShiftId() ? 'Guardar' : 'Agregar' }}
                  </button>
                  @if (editingShiftId()) {
                    <button mat-button type="button" (click)="resetShiftForm()">Cancelar</button>
                  }
                </form>

                <table mat-table [dataSource]="shifts()" style="width:100%;margin-top:12px">
                  <ng-container matColumnDef="name">
                    <th mat-header-cell *matHeaderCellDef>Turno</th>
                    <td mat-cell *matCellDef="let s">{{ s.name }}</td>
                  </ng-container>
                  <ng-container matColumnDef="time">
                    <th mat-header-cell *matHeaderCellDef>Horario</th>
                    <td mat-cell *matCellDef="let s">{{ s.startTime }}–{{ s.endTime }}</td>
                  </ng-container>
                  <ng-container matColumnDef="tolerance">
                    <th mat-header-cell *matHeaderCellDef>Tol.</th>
                    <td mat-cell *matCellDef="let s">{{ s.lateToleranceMin }}m</td>
                  </ng-container>
                  <ng-container matColumnDef="actions">
                    <th mat-header-cell *matHeaderCellDef></th>
                    <td mat-cell *matCellDef="let s" style="text-align:right">
                      <button mat-icon-button (click)="editShift(s)" aria-label="Editar"><mat-icon>edit</mat-icon></button>
                    </td>
                  </ng-container>
                  <tr mat-header-row *matHeaderRowDef="shiftColumns"></tr>
                  <tr mat-row *matRowDef="let row; columns: shiftColumns"></tr>
                </table>
              }
            </mat-card-content>
          </mat-card>
        </div>
      </mat-tab>

      <!-- Asignaciones -->
      <mat-tab label="Asignaciones">
        <mat-card style="margin-top:16px">
          <mat-card-content>
            <form [formGroup]="assignForm" (ngSubmit)="assign()" style="display:flex;gap:8px;flex-wrap:wrap;align-items:baseline">
              <mat-form-field appearance="outline" style="width:240px">
                <mat-label>Usuario</mat-label>
                <mat-select formControlName="userId">
                  @for (u of users(); track u.id) {
                    <mat-option [value]="u.id">
                      {{ u.firstName }} {{ u.lastName }}@if (u.employeeCode) { ({{ u.employeeCode }}) }
                    </mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:200px">
                <mat-label>Horario</mat-label>
                <mat-select formControlName="scheduleId"
                            (selectionChange)="onAssignScheduleChange($event.value)">
                  @for (s of schedules(); track s.id) {
                    <mat-option [value]="s.id">{{ s.name }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:220px">
                <mat-label>Turno</mat-label>
                <mat-select formControlName="shiftId">
                  @for (s of assignShifts(); track s.id) {
                    <mat-option [value]="s.id">{{ s.name }} ({{ s.startTime }}–{{ s.endTime }})</mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:220px">
                <mat-label>Centro (opcional)</mat-label>
                <mat-select formControlName="workSiteId">
                  <mat-option [value]="''">—</mat-option>
                  @for (w of workSites(); track w.id) {
                    <mat-option [value]="w.id">{{ w.name }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:160px">
                <mat-label>Desde</mat-label>
                <input matInput type="date" formControlName="validFrom" />
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:160px">
                <mat-label>Hasta (opcional)</mat-label>
                <input matInput type="date" formControlName="validTo" />
              </mat-form-field>
              <button mat-flat-button color="primary" type="submit" [disabled]="assignForm.invalid">Asignar</button>
            </form>

            <div style="display:flex;gap:8px;align-items:baseline;margin-top:12px">
              <mat-form-field appearance="outline" style="width:280px">
                <mat-label>Listar asignaciones por usuario</mat-label>
                <mat-select [formControl]="lookupUserId">
                  @for (u of users(); track u.id) {
                    <mat-option [value]="u.id">
                      {{ u.firstName }} {{ u.lastName }}@if (u.employeeCode) { ({{ u.employeeCode }}) }
                    </mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <button mat-stroked-button type="button" (click)="loadAssignments()">Buscar</button>
            </div>

            <table mat-table [dataSource]="assignments()" style="width:100%;margin-top:12px">
              <ng-container matColumnDef="shiftId">
                <th mat-header-cell *matHeaderCellDef>Turno</th>
                <td mat-cell *matCellDef="let a">{{ shiftLabel(a.shiftId) }}</td>
              </ng-container>
              <ng-container matColumnDef="workSiteId">
                <th mat-header-cell *matHeaderCellDef>Centro</th>
                <td mat-cell *matCellDef="let a">{{ workSiteLabel(a.workSiteId) }}</td>
              </ng-container>
              <ng-container matColumnDef="range">
                <th mat-header-cell *matHeaderCellDef>Vigencia</th>
                <td mat-cell *matCellDef="let a">{{ a.validFrom }} → {{ a.validTo || '—' }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="assignmentColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: assignmentColumns"></tr>
            </table>
          </mat-card-content>
        </mat-card>
      </mat-tab>
    </mat-tab-group>
  `,
})
export class SchedulingComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(SchedulingService);
  private readonly userService = inject(UserService);
  private readonly workSiteService = inject(WorkSiteService);
  private readonly notify = inject(NotificationService);

  protected readonly scheduleStatuses = SCHEDULE_STATUSES;
  protected readonly scheduleColumns = ['code', 'name', 'status', 'actions'];
  protected readonly shiftColumns = ['name', 'time', 'tolerance', 'actions'];
  protected readonly assignmentColumns = ['shiftId', 'workSiteId', 'range'];

  protected readonly schedules = signal<Schedule[]>([]);
  protected readonly shifts = signal<Shift[]>([]);
  protected readonly assignments = signal<Assignment[]>([]);
  protected readonly users = signal<User[]>([]);
  protected readonly workSites = signal<WorkSite[]>([]);
  protected readonly assignShifts = signal<Shift[]>([]);
  protected readonly shiftsById = signal<Record<string, Shift>>({});
  protected readonly selectedSchedule = signal<Schedule | null>(null);
  protected readonly editingScheduleId = signal<string | null>(null);
  protected readonly editingShiftId = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly scheduleForm = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    name: ['', [Validators.required]],
    timezone: [''],
    status: ['ACTIVE'],
  });

  protected readonly shiftForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    startTime: ['08:00', [Validators.required]],
    endTime: ['17:00', [Validators.required]],
    lateToleranceMin: [10],
    crossesMidnight: [false],
  });

  protected readonly assignForm = this.fb.nonNullable.group({
    userId: ['', [Validators.required]],
    scheduleId: [''],
    shiftId: ['', [Validators.required]],
    workSiteId: [''],
    validFrom: ['', [Validators.required]],
    validTo: [''],
  });

  protected readonly lookupUserId = this.fb.nonNullable.control('');

  constructor() {
    this.assignForm.controls.shiftId.disable();
    this.reloadSchedules();
    this.reloadLookups();
  }

  private reloadLookups(): void {
    this.userService.list(0, 200).subscribe({
      next: (result) => this.users.set(result.content),
      error: () => this.error.set('No se pudo cargar la lista de usuarios.'),
    });
    this.workSiteService.list(0, 200).subscribe({
      next: (result) => this.workSites.set(result.content),
      error: () => this.error.set('No se pudo cargar la lista de centros de trabajo.'),
    });
  }

  /** Puebla el desplegable de turnos del formulario de asignación al elegir un horario. */
  protected onAssignScheduleChange(scheduleId: string): void {
    this.assignForm.controls.shiftId.reset('');
    if (!scheduleId) {
      this.assignShifts.set([]);
      this.assignForm.controls.shiftId.disable();
      return;
    }
    this.assignForm.controls.shiftId.enable();
    this.service.listShifts(scheduleId).subscribe({
      next: (shifts) => this.assignShifts.set(shifts),
      error: () => this.notify.error('No se pudieron cargar los turnos del horario.'),
    });
  }

  private reloadSchedules(): void {
    this.service.listSchedules(0, 100).subscribe({
      next: (result) => {
        this.schedules.set(result.content);
        this.reloadShiftIndex(result.content);
      },
      error: () => this.error.set('No se pudo cargar horarios (¿permiso schedule:manage?).'),
    });
  }

  /** Construye el índice de turnos por id (de todos los horarios) para resolver nombres en la tabla de asignaciones. */
  private reloadShiftIndex(schedules: Schedule[]): void {
    if (!schedules.length) {
      this.shiftsById.set({});
      return;
    }
    forkJoin(schedules.map((s) => this.service.listShifts(s.id))).subscribe({
      next: (lists) => {
        const index: Record<string, Shift> = {};
        for (const list of lists) {
          for (const shift of list) {
            index[shift.id] = shift;
          }
        }
        this.shiftsById.set(index);
      },
      error: () => {},
    });
  }

  protected shiftLabel(shiftId: string): string {
    const shift = this.shiftsById()[shiftId];
    return shift ? `${shift.name} (${shift.startTime.substring(0, 5)}–${shift.endTime.substring(0, 5)})` : shiftId;
  }

  protected workSiteLabel(workSiteId?: string): string {
    if (!workSiteId) {
      return '—';
    }
    return this.workSites().find((w) => w.id === workSiteId)?.name ?? workSiteId;
  }

  protected saveSchedule(): void {
    if (this.scheduleForm.invalid) {
      return;
    }
    const raw = this.scheduleForm.getRawValue();
    const editId = this.editingScheduleId();
    const request$ = editId
      ? this.service.updateSchedule(editId, {
          name: raw.name,
          timezone: raw.timezone || undefined,
          status: raw.status as Schedule['status'],
        })
      : this.service.createSchedule({ code: raw.code, name: raw.name, timezone: raw.timezone || undefined });
    request$.subscribe({
      next: () => {
        this.notify.success(editId ? 'Horario actualizado.' : 'Horario creado.');
        this.resetScheduleForm();
        this.reloadSchedules();
      },
      error: () => this.notify.error('No se pudo guardar el horario.'),
    });
  }

  protected editSchedule(schedule: Schedule): void {
    this.editingScheduleId.set(schedule.id);
    this.scheduleForm.reset({
      code: schedule.code,
      name: schedule.name,
      timezone: schedule.timezone ?? '',
      status: schedule.status,
    });
    this.scheduleForm.controls.code.disable();
  }

  protected resetScheduleForm(): void {
    this.editingScheduleId.set(null);
    this.scheduleForm.controls.code.enable();
    this.scheduleForm.reset({ status: 'ACTIVE' });
  }

  protected selectSchedule(schedule: Schedule): void {
    this.selectedSchedule.set(schedule);
    this.resetShiftForm();
    this.service.listShifts(schedule.id).subscribe({
      next: (shifts) => this.shifts.set(shifts),
      error: () => this.notify.error('No se pudieron cargar los turnos.'),
    });
  }

  protected saveShift(): void {
    const schedule = this.selectedSchedule();
    if (!schedule || this.shiftForm.invalid) {
      return;
    }
    const raw = this.shiftForm.getRawValue();
    const payload = {
      name: raw.name,
      startTime: raw.startTime,
      endTime: raw.endTime,
      lateToleranceMin: raw.lateToleranceMin ?? undefined,
      crossesMidnight: raw.crossesMidnight,
    };
    const editId = this.editingShiftId();
    const request$ = editId
      ? this.service.updateShift(editId, payload)
      : this.service.createShift(schedule.id, payload);
    request$.subscribe({
      next: () => {
        this.notify.success(editId ? 'Turno actualizado.' : 'Turno agregado.');
        this.resetShiftForm();
        this.selectSchedule(schedule);
      },
      error: () => this.notify.error('No se pudo guardar el turno.'),
    });
  }

  protected editShift(shift: Shift): void {
    this.editingShiftId.set(shift.id);
    this.shiftForm.reset({
      name: shift.name,
      startTime: shift.startTime.substring(0, 5),
      endTime: shift.endTime.substring(0, 5),
      lateToleranceMin: shift.lateToleranceMin,
      crossesMidnight: shift.crossesMidnight,
    });
  }

  protected resetShiftForm(): void {
    this.editingShiftId.set(null);
    this.shiftForm.reset({ startTime: '08:00', endTime: '17:00', lateToleranceMin: 10, crossesMidnight: false });
  }

  protected assign(): void {
    if (this.assignForm.invalid) {
      return;
    }
    const raw = this.assignForm.getRawValue();
    this.service
      .assign({
        userId: raw.userId,
        shiftId: raw.shiftId,
        workSiteId: raw.workSiteId || undefined,
        validFrom: raw.validFrom,
        validTo: raw.validTo || undefined,
      })
      .subscribe({
        next: () => {
          this.notify.success('Asignación creada.');
          this.assignForm.reset();
          this.assignShifts.set([]);
          this.assignForm.controls.shiftId.disable();
        },
        error: () => this.notify.error('No se pudo crear la asignación.'),
      });
  }

  protected loadAssignments(): void {
    const userId = this.lookupUserId.value.trim();
    if (!userId) {
      return;
    }
    this.service.listAssignments(userId).subscribe({
      next: (list) => this.assignments.set(list),
      error: () => this.notify.error('No se pudieron cargar las asignaciones.'),
    });
  }
}
