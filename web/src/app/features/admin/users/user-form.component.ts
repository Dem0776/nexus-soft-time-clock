import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';

import { AuthStore } from '../../../core/auth/auth.store';
import { BreadcrumbComponent } from '../../../core/ui/breadcrumb.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { rankOf, Role } from '../roles/role.models';
import { RoleService } from '../roles/role.service';
import { USER_STATUSES, User } from './user.models';
import { UserService } from './user.service';

/**
 * Alta / edición de usuario en página completa (con breadcrumb), reemplazando el drawer
 * anterior para igualar el patrón "Formulario" de la guía. El backend solo expone
 * PATCH /status y PUT /roles para un usuario existente — no hay endpoint para editar
 * nombre/correo/código, así que en modo edición esos campos se muestran de solo lectura.
 */
@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    BreadcrumbComponent,
    PageHeaderComponent,
    StatusChipComponent,
  ],
  template: `
    <app-breadcrumb
      [items]="[
        { label: 'Organización', route: '/users' },
        { label: 'Usuarios', route: '/users' },
        { label: isEdit() ? (user()?.firstName ?? '') + ' ' + (user()?.lastName ?? '') : 'Nuevo usuario' },
      ]"
    />
    <app-page-header [title]="isEdit() ? 'Editar usuario' : 'Nuevo usuario'" />

    @if (loading()) { <mat-progress-bar mode="indeterminate" style="margin-bottom:16px" /> }
    @if (error()) { <p class="error-text">{{ error() }}</p> }

    <div class="form-page-layout">
      <div class="form-page-main">
        @if (!isEdit()) {
          <h3 class="form-section-title">Información del usuario</h3>
          <form [formGroup]="createForm">
            <div class="form-grid-2">
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Nombre(s)</mat-label>
                <input matInput formControlName="firstName" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Apellidos</mat-label>
                <input matInput formControlName="lastName" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Correo electrónico</mat-label>
                <input matInput type="email" formControlName="email" />
                <mat-hint>Se usará para iniciar sesión.</mat-hint>
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Código de empleado</mat-label>
                <input matInput formControlName="employeeCode" />
                <mat-hint>Código interno opcional para reportes.</mat-hint>
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Contraseña</mat-label>
                <input matInput type="password" formControlName="password" autocomplete="new-password" />
              </mat-form-field>
            </div>
          </form>
        } @else {
          @if (user(); as u) {
            <h3 class="form-section-title">Información del usuario</h3>
            <div class="form-grid-2">
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Nombre(s)</mat-label>
                <input matInput [value]="u.firstName" readonly disabled />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Apellidos</mat-label>
                <input matInput [value]="u.lastName" readonly disabled />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Correo electrónico</mat-label>
                <input matInput [value]="u.email" readonly disabled />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Código de empleado</mat-label>
                <input matInput [value]="u.employeeCode || '—'" readonly disabled />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Estado</mat-label>
                <mat-select [formControl]="statusControl">
                  @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
                </mat-select>
              </mat-form-field>
            </div>
          }
        }

        <div class="form-section">
          <h3 class="form-section-title">Roles del usuario</h3>
          <p class="muted" style="margin-top:-8px">Selecciona los roles que tendrá el usuario en el sistema.</p>
          @if (assignableRoles().length === 0) {
            <p class="muted">No tenés roles asignables (no podés otorgar roles de mayor privilegio que el propio).</p>
          } @else {
            <div class="dual-list">
              <div class="dual-list-col">
                <p class="dual-list-title">Roles disponibles</p>
                <mat-form-field appearance="outline" class="drawer-field">
                  <mat-icon matPrefix>search</mat-icon>
                  <input matInput [formControl]="roleSearch" placeholder="Buscar rol..." />
                </mat-form-field>
                <div class="dual-list-box">
                  @for (role of availableRoles(); track role.code) {
                    <label class="dual-list-item">
                      <mat-checkbox
                        [checked]="checkedAvailable().has(role.code)"
                        (change)="setChecked(checkedAvailable, role.code, $event.checked)"
                      />
                      <span style="flex:1 1 auto">{{ role.name }}</span>
                      <span class="rank-badge" [class]="'tier-' + rankTier(role.code)">Rango {{ rank(role.code) }}</span>
                    </label>
                  }
                  @if (availableRoles().length === 0) { <p class="muted" style="padding:var(--sp-3)">Sin resultados.</p> }
                </div>
              </div>
              <div class="dual-list-arrows">
                <button mat-icon-button type="button" [disabled]="checkedAvailable().size === 0" (click)="moveToAssigned()" aria-label="Asignar seleccionados">
                  <mat-icon>chevron_right</mat-icon>
                </button>
                <button mat-icon-button type="button" [disabled]="checkedAssigned().size === 0" (click)="moveToAvailable()" aria-label="Quitar seleccionados">
                  <mat-icon>chevron_left</mat-icon>
                </button>
              </div>
              <div class="dual-list-col">
                <p class="dual-list-title">Roles asignados</p>
                <div class="dual-list-box">
                  @for (code of pendingRoles(); track code) {
                    <label class="dual-list-item assigned">
                      <mat-checkbox
                        [checked]="checkedAssigned().has(code)"
                        (change)="setChecked(checkedAssigned, code, $event.checked)"
                      />
                      <span style="flex:1 1 auto">{{ roleName(code) }}</span>
                      <span class="rank-badge" [class]="'tier-' + rankTier(code)">{{ rank(code) }}</span>
                    </label>
                  }
                  @if (pendingRoles().length === 0) { <p class="muted" style="padding:var(--sp-3)">Ningún rol asignado.</p> }
                </div>
              </div>
            </div>
          }
          <div class="form-callout">
            <mat-icon>info</mat-icon>
            <p class="row-desc" style="margin:0">No es posible asignar roles con un rango igual o superior al tuyo (Rango {{ ownRank() }}).</p>
          </div>
        </div>

        <div class="form-actions">
          <button mat-button type="button" (click)="cancel()">Cancelar</button>
          <button mat-flat-button color="primary" [disabled]="!canSave() || saving()" (click)="save()">
            <mat-icon>save</mat-icon> {{ isEdit() ? 'Guardar cambios' : 'Guardar usuario' }}
          </button>
        </div>
      </div>

      <div class="form-page-side">
        <div class="info-card tip">
          <h3 class="info-card-title">Reglas de roles</h3>
          <div class="icon-row">
            <mat-icon>shield</mat-icon>
            <div>
              <p class="row-title">Jerarquía de roles</p>
              <p class="row-desc">No puedes asignar a un usuario roles con un rango igual o superior al tuyo.</p>
            </div>
          </div>
        </div>

        <div class="info-card">
          <h3 class="info-card-title">Jerarquía de roles</h3>
          @for (role of assignableRoles(); track role.code) {
            <div class="info-row"><span class="k">{{ role.name }}</span><span class="v"><span class="rank-badge" [class]="'tier-' + rankTier(role.code)">{{ rank(role.code) }}</span></span></div>
          }
        </div>

        @if (isEdit() && user(); as u) {
          <div class="info-card">
            <div class="entity-avatar-row">
              <div class="entity-avatar">{{ initials(u) }}</div>
              <div>
                <p class="entity-avatar-name">{{ u.firstName }} {{ u.lastName }}</p>
                <app-status-chip [status]="u.status" />
              </div>
            </div>
            <div class="info-row"><span class="k">Correo</span><span class="v">{{ u.email }}</span></div>
            <div class="info-row"><span class="k">Código</span><span class="v">{{ u.employeeCode || '—' }}</span></div>
          </div>
        }
      </div>
    </div>
  `,
})
export class UserFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly notify = inject(NotificationService);
  private readonly authStore = inject(AuthStore);

  protected readonly statuses = USER_STATUSES;
  protected readonly userId = this.route.snapshot.paramMap.get('id');
  protected readonly isEdit = computed(() => !!this.userId);

  protected readonly user = signal<User | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly assignableRoles = signal<Role[]>([]);
  protected readonly pendingRoles = signal<string[]>([]);
  protected readonly statusControl = this.fb.nonNullable.control('ACTIVE');
  protected readonly roleSearch = this.fb.nonNullable.control('');
  protected readonly checkedAvailable = signal<Set<string>>(new Set());
  protected readonly checkedAssigned = signal<Set<string>>(new Set());

  protected readonly rank = rankOf;

  protected initials(u: User): string {
    return `${u.firstName.charAt(0)}${u.lastName.charAt(0)}`.toUpperCase();
  }

  protected setChecked(target: typeof this.checkedAvailable, code: string, checked: boolean): void {
    target.update((set) => {
      const next = new Set(set);
      if (checked) {
        next.add(code);
      } else {
        next.delete(code);
      }
      return next;
    });
  }

  protected moveToAssigned(): void {
    const toAdd = this.checkedAvailable();
    this.pendingRoles.update((list) => [...new Set([...list, ...toAdd])]);
    this.checkedAvailable.set(new Set());
  }

  protected moveToAvailable(): void {
    const toRemove = this.checkedAssigned();
    this.pendingRoles.update((list) => list.filter((code) => !toRemove.has(code)));
    this.checkedAssigned.set(new Set());
  }

  protected readonly ownRank = computed(() => {
    const roles = this.authStore.user()?.roles ?? [];
    return roles.length ? Math.max(...roles.map(rankOf)) : 0;
  });

  private readonly roleSearchValue = toSignal(this.roleSearch.valueChanges, { initialValue: '' });
  protected readonly availableRoles = computed(() => {
    const assigned = new Set(this.pendingRoles());
    const search = this.roleSearchValue().trim().toLowerCase();
    return this.assignableRoles().filter(
      (role) => !assigned.has(role.code) && (!search || role.name.toLowerCase().includes(search)),
    );
  });

  protected roleName(code: string): string {
    return this.assignableRoles().find((role) => role.code === code)?.name ?? code;
  }

  protected rankTier(code: string): 1 | 2 | 3 | 4 {
    const value = rankOf(code);
    if (value >= 80) return 1;
    if (value >= 60) return 2;
    if (value >= 40) return 3;
    return 4;
  }

  protected readonly createForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    employeeCode: [''],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  private readonly createFormStatus = toSignal(this.createForm.statusChanges, { initialValue: this.createForm.status });
  protected readonly canSave = computed(() => (this.isEdit() ? true : this.createFormStatus() === 'VALID'));

  constructor() {
    this.roleService.list().subscribe({
      next: (roles) => this.assignableRoles.set(roles),
      error: () => void 0,
    });
    if (this.userId) {
      this.loading.set(true);
      this.service.get(this.userId).subscribe({
        next: (u) => {
          this.user.set(u);
          this.pendingRoles.set([...u.roles]);
          this.statusControl.setValue(u.status);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el usuario.');
          this.loading.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    void this.router.navigate(['/users']);
  }

  protected save(): void {
    if (this.isEdit()) {
      this.saveEdit();
    } else {
      this.saveCreate();
    }
  }

  private saveCreate(): void {
    if (this.createForm.invalid) {
      return;
    }
    const raw = this.createForm.getRawValue();
    this.saving.set(true);
    this.service
      .create({
        email: raw.email,
        firstName: raw.firstName,
        lastName: raw.lastName,
        employeeCode: raw.employeeCode || undefined,
        password: raw.password,
        roleCodes: this.pendingRoles().length ? this.pendingRoles() : undefined,
      })
      .subscribe({
        next: () => {
          this.notify.success('Usuario creado.');
          void this.router.navigate(['/users']);
        },
        error: () => {
          this.saving.set(false);
          this.notify.error('No se pudo crear el usuario.');
        },
      });
  }

  private saveEdit(): void {
    const u = this.user();
    if (!u) {
      return;
    }
    this.saving.set(true);
    const statusChanged = this.statusControl.value !== u.status;
    const rolesChanged = JSON.stringify([...this.pendingRoles()].sort()) !== JSON.stringify([...u.roles].sort());

    const requests: Array<ReturnType<typeof this.service.updateStatus>> = [];
    if (statusChanged) {
      requests.push(this.service.updateStatus(u.id, this.statusControl.value as User['status']));
    }
    if (rolesChanged) {
      requests.push(this.service.assignRoles(u.id, this.pendingRoles()));
    }
    if (requests.length === 0) {
      this.saving.set(false);
      void this.router.navigate(['/users']);
      return;
    }
    let remaining = requests.length;
    let failed = false;
    requests.forEach((req$) =>
      req$.subscribe({
        next: () => {
          remaining--;
          if (remaining === 0) {
            this.saving.set(false);
            if (failed) {
              this.notify.error('Algunos cambios no se pudieron guardar.');
            } else {
              this.notify.success('Cambios guardados.');
              void this.router.navigate(['/users']);
            }
          }
        },
        error: () => {
          remaining--;
          failed = true;
          if (remaining === 0) {
            this.saving.set(false);
            this.notify.error('Algunos cambios no se pudieron guardar.');
          }
        },
      }),
    );
  }
}
