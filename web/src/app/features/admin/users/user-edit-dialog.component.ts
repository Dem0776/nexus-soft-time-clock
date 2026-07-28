import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin, of } from 'rxjs';

import { NotificationService } from '../../../core/ui/notification.service';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { rankOf, Role } from '../roles/role.models';
import { EmployeeProfile, GENDERS } from './employee-profile.models';
import { EmployeeProfileService } from './employee-profile.service';
import { USER_STATUSES, User, UserStatus } from './user.models';
import { UserService } from './user.service';

export interface UserEditData {
  user: User;
  assignableRoles: Role[];
}

/**
 * Edición de usuario en modal: datos personales (perfil), estado y roles en un solo lugar,
 * consistente con el alta. Nombre/correo/código son de solo lectura (los gestiona identity).
 * Guarda el perfil (PUT /users/{id}/profile) y, si cambian, estado y roles.
 */
@Component({
  selector: 'app-user-edit-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatProgressBarModule,
    StatusChipComponent,
  ],
  template: `
    <div class="m-head">
      <div class="badge-ic">{{ initials }}</div>
      <div class="t">
        <h2>{{ data.user.firstName }} {{ data.user.lastName }}</h2>
        <p>{{ data.user.email }}<span class="sep">·</span>{{ data.user.employeeCode || 'sin código' }}</p>
      </div>
      <button mat-icon-button (click)="close(false)" aria-label="Cerrar"><mat-icon>close</mat-icon></button>
    </div>

    @if (loading()) { <mat-progress-bar mode="indeterminate" /> }

    <form [formGroup]="form" class="m-body">
      <!-- Datos personales (perfil) -->
      <section class="fs">
        <div class="fs-head"><mat-icon>person</mat-icon><h4>Datos personales</h4><span class="opt">Opcional</span></div>
        <div class="grid2">
          <mat-form-field appearance="outline">
            <mat-label>Género</mat-label>
            <mat-select formControlName="gender">
              <mat-option [value]="''">Sin especificar</mat-option>
              @for (g of genders; track g.value) { <mat-option [value]="g.value">{{ g.label }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Teléfono</mat-label>
            <mat-icon matPrefix>call</mat-icon>
            <input matInput type="tel" formControlName="phone" placeholder="+52 55 1234 5678" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Fecha de nacimiento</mat-label>
            <input matInput type="date" formControlName="birthDate" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Fecha de ingreso</mat-label>
            <input matInput type="date" formControlName="hireDate" />
            <mat-hint>Determina la antigüedad y los días de vacaciones.</mat-hint>
          </mat-form-field>
        </div>

        <mat-accordion class="opt-acc">
          <mat-expansion-panel [expanded]="hasEmergency()">
            <mat-expansion-panel-header>
              <mat-panel-title><mat-icon>call</mat-icon> Contacto de emergencia</mat-panel-title>
            </mat-expansion-panel-header>
            <div class="grid2">
              <mat-form-field appearance="outline">
                <mat-label>Nombre del contacto</mat-label>
                <input matInput formControlName="emergencyContactName" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Teléfono del contacto</mat-label>
                <mat-icon matPrefix>call</mat-icon>
                <input matInput type="tel" formControlName="emergencyContactPhone" />
              </mat-form-field>
            </div>
          </mat-expansion-panel>
          <mat-expansion-panel [expanded]="!!form.controls.address.value">
            <mat-expansion-panel-header>
              <mat-panel-title><mat-icon>place</mat-icon> Dirección</mat-panel-title>
            </mat-expansion-panel-header>
            <mat-form-field appearance="outline" class="full">
              <mat-label>Dirección</mat-label>
              <textarea matInput rows="2" formControlName="address" placeholder="Calle, número, colonia, ciudad, C.P."></textarea>
            </mat-form-field>
          </mat-expansion-panel>
        </mat-accordion>
      </section>

      <!-- Estado -->
      <section class="fs">
        <div class="fs-head"><mat-icon>badge</mat-icon><h4>Cuenta</h4></div>
        <div class="grid2">
          <mat-form-field appearance="outline">
            <mat-label>Estado</mat-label>
            <mat-select formControlName="status">
              @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <div class="chip-cell"><app-status-chip [status]="form.controls.status.value" /></div>
        </div>
      </section>

      <!-- Roles -->
      <section class="fs">
        <div class="fs-head"><mat-icon>shield</mat-icon><h4>Roles</h4></div>
        @if (data.assignableRoles.length === 0) {
          <p class="muted">No tienes roles asignables (no puedes otorgar roles de mayor privilegio que el propio).</p>
        }
        <div class="roles-list">
          @for (role of data.assignableRoles; track role.code) {
            <mat-checkbox [checked]="isRole(role.code)" (change)="toggleRole(role.code, $event.checked)">
              {{ role.name }} <span class="rank-badge" [class]="'tier-' + rankTier(role.code)">Rango {{ rank(role.code) }}</span>
            </mat-checkbox>
          }
        </div>
      </section>
    </form>

    <div class="m-foot">
      <span class="fill"></span>
      <button mat-button (click)="close(false)">Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="saving()" (click)="submit()">
        <mat-icon>save</mat-icon> Guardar cambios
      </button>
    </div>
  `,
  styles: [
    `
      :host { display: block; }
      .m-head { display: flex; align-items: center; gap: 14px; padding: 4px 4px 16px; border-bottom: 1px solid var(--border); }
      .m-head .badge-ic { width: 44px; height: 44px; border-radius: 50%; background: var(--brand-soft); color: var(--brand); display: grid; place-items: center; border: 1px solid var(--brand-border); flex: none; font-weight: 700; }
      .m-head .t { flex: 1; }
      .m-head h2 { margin: 0; font-size: 1.15rem; font-weight: 700; }
      .m-head p { margin: 2px 0 0; color: var(--text-muted); font-size: var(--font-small); }
      .m-head .sep { margin: 0 6px; }
      .m-body { padding: 4px 4px 8px; max-height: 60vh; overflow-y: auto; }
      .fs { padding: 16px 0; }
      .fs + .fs { border-top: 1px solid var(--border); }
      .fs-head { display: flex; align-items: center; gap: 9px; margin-bottom: 12px; }
      .fs-head mat-icon { color: var(--brand); }
      .fs-head h4 { margin: 0; font-size: var(--font-body); font-weight: 700; }
      .fs-head .opt { margin-left: 4px; font-size: var(--font-caption); font-weight: 700; text-transform: uppercase; letter-spacing: .05em; color: var(--text-soft); border: 1px solid var(--border); border-radius: 999px; padding: 2px 8px; }
      .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-4); align-items: center; }
      .grid2 .full { grid-column: 1 / -1; }
      mat-form-field { width: 100%; }
      .chip-cell { display: flex; align-items: center; }
      .opt-acc { margin-top: 6px; display: block; }
      .roles-list { display: flex; flex-direction: column; gap: var(--sp-2); }
      .m-foot { display: flex; align-items: center; gap: 10px; padding: 14px 4px 4px; border-top: 1px solid var(--border); }
      .m-foot .fill { flex: 1; }
      @media (max-width: 640px) { .grid2 { grid-template-columns: 1fr; } }
    `,
  ],
})
export class UserEditDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly profileService = inject(EmployeeProfileService);
  private readonly notify = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<UserEditDialogComponent, boolean>);
  protected readonly data = inject<UserEditData>(MAT_DIALOG_DATA);

  protected readonly genders = GENDERS;
  protected readonly statuses = USER_STATUSES;
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly roles = signal<string[]>([...this.data.user.roles]);
  protected readonly rank = rankOf;

  protected rankTier(code: string): 1 | 2 | 3 | 4 {
    const value = rankOf(code);
    if (value >= 80) return 1;
    if (value >= 60) return 2;
    if (value >= 40) return 3;
    return 4;
  }

  protected readonly form = this.fb.nonNullable.group({
    gender: [''],
    phone: [''],
    birthDate: [''],
    hireDate: [''],
    emergencyContactName: [''],
    emergencyContactPhone: [''],
    address: [''],
    status: [this.data.user.status as UserStatus],
  });

  get initials(): string {
    const f = this.data.user.firstName?.[0] ?? '';
    const l = this.data.user.lastName?.[0] ?? '';
    return (f + l).toUpperCase() || '?';
  }

  constructor() {
    this.profileService.get(this.data.user.id).subscribe({
      next: (p) => {
        this.form.patchValue({
          gender: p.gender ?? '',
          phone: p.phone ?? '',
          birthDate: p.birthDate ?? '',
          hireDate: p.hireDate ?? '',
          emergencyContactName: p.emergencyContactName ?? '',
          emergencyContactPhone: p.emergencyContactPhone ?? '',
          address: p.address ?? '',
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected hasEmergency(): boolean {
    const v = this.form.controls;
    return !!(v.emergencyContactName.value || v.emergencyContactPhone.value);
  }

  protected isRole(code: string): boolean {
    return this.roles().includes(code);
  }

  protected toggleRole(code: string, checked: boolean): void {
    this.roles.update((list) => (checked ? [...new Set([...list, code])] : list.filter((c) => c !== code)));
  }

  protected close(result: boolean): void {
    this.dialogRef.close(result);
  }

  protected submit(): void {
    const raw = this.form.getRawValue();
    const profile: EmployeeProfile = {
      birthDate: raw.birthDate || undefined,
      hireDate: raw.hireDate || undefined,
      gender: (raw.gender as EmployeeProfile['gender']) || undefined,
      phone: raw.phone || undefined,
      address: raw.address || undefined,
      emergencyContactName: raw.emergencyContactName || undefined,
      emergencyContactPhone: raw.emergencyContactPhone || undefined,
    };

    const ops = [this.profileService.save(this.data.user.id, profile)];
    if (raw.status !== this.data.user.status) {
      ops.push(this.userService.updateStatus(this.data.user.id, raw.status) as never);
    }
    if (!sameSet(this.roles(), this.data.user.roles)) {
      ops.push(this.userService.assignRoles(this.data.user.id, this.roles()) as never);
    }

    this.saving.set(true);
    forkJoin(ops.length ? ops : [of(null)]).subscribe({
      next: () => {
        this.saving.set(false);
        this.notify.success('Usuario actualizado.');
        this.close(true);
      },
      error: () => {
        this.saving.set(false);
        this.notify.error('No se pudieron guardar todos los cambios.');
      },
    });
  }
}

function sameSet(a: string[], b: string[]): boolean {
  if (a.length !== b.length) return false;
  const sb = new Set(b);
  return a.every((x) => sb.has(x));
}
