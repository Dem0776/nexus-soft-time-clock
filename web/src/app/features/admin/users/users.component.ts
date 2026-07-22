import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { AuthStore } from '../../../core/auth/auth.store';
import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { Role } from '../roles/role.models';
import { RoleService } from '../roles/role.service';
import { AssignRolesDialogComponent } from './assign-roles-dialog.component';
import { USER_STATUSES, User, UserStatus } from './user.models';
import { UserService } from './user.service';

/** Administración de usuarios del tenant (RF-06, RF-22): alta, listado, estado y asignación de roles. */
@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressBarModule,
    MatDialogModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Usuarios">
      <button mat-flat-button color="primary" (click)="showForm.set(!showForm())">
        <mat-icon>person_add</mat-icon> Nuevo usuario
      </button>
    </app-page-header>

    @if (showForm()) {
      <mat-card style="margin-bottom:16px">
        <mat-card-header><mat-card-title>Nuevo usuario</mat-card-title></mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="create()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:baseline">
            <mat-form-field appearance="outline">
              <mat-label>Correo</mat-label>
              <input matInput type="email" formControlName="email" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Nombre</mat-label>
              <input matInput formControlName="firstName" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Apellido</mat-label>
              <input matInput formControlName="lastName" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Código de empleado</mat-label>
              <input matInput formControlName="employeeCode" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Contraseña</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="new-password" />
            </mat-form-field>
            <mat-form-field appearance="outline" style="min-width:220px">
              <mat-label>Roles</mat-label>
              <mat-select formControlName="roleCodes" multiple>
                @for (role of assignableRoles(); track role.code) {
                  <mat-option [value]="role.code">{{ role.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || loading()">Crear</button>
            <button mat-button type="button" (click)="showForm.set(false)">Cancelar</button>
          </form>
        </mat-card-content>
      </mat-card>
    }

    <mat-card>
      <mat-card-content>
        <form (ngSubmit)="applySearch()" style="display:flex;gap:12px;align-items:baseline">
          <mat-form-field appearance="outline" style="flex:1 1 320px">
            <mat-label>Buscar</mat-label>
            <input matInput [formControl]="searchControl" placeholder="correo o nombre" />
          </mat-form-field>
          <button mat-stroked-button type="submit">Buscar</button>
        </form>

        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <table mat-table [dataSource]="users()" style="width:100%">
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Correo</th>
            <td mat-cell *matCellDef="let u">{{ u.email }}</td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let u">{{ u.firstName }} {{ u.lastName }}</td>
          </ng-container>
          <ng-container matColumnDef="employeeCode">
            <th mat-header-cell *matHeaderCellDef>Código</th>
            <td mat-cell *matCellDef="let u">{{ u.employeeCode || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Estado</th>
            <td mat-cell *matCellDef="let u"><app-status-chip [status]="u.status" /></td>
          </ng-container>
          <ng-container matColumnDef="roles">
            <th mat-header-cell *matHeaderCellDef>Roles</th>
            <td mat-cell *matCellDef="let u">{{ u.roles.join(', ') || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let u" style="text-align:right;white-space:nowrap">
              <button mat-icon-button (click)="openRoles(u)" aria-label="Roles"><mat-icon>badge</mat-icon></button>
              <button mat-icon-button [matMenuTriggerFor]="statusMenu" aria-label="Estado"><mat-icon>more_vert</mat-icon></button>
              <mat-menu #statusMenu="matMenu">
                @for (s of statuses; track s) {
                  <button mat-menu-item [disabled]="s === u.status" (click)="changeStatus(u, s)">{{ s }}</button>
                }
              </mat-menu>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>

        @if (!loading() && users().length === 0) {
          <app-empty-state icon="group" message="No hay usuarios para mostrar." />
        }

        <mat-paginator
          [length]="total()"
          [pageSize]="size()"
          [pageIndex]="page()"
          [pageSizeOptions]="[10, 20, 50]"
          (page)="onPage($event)"
        />
      </mat-card-content>
    </mat-card>
  `,
})
export class UsersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly store = inject(AuthStore);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['email', 'name', 'employeeCode', 'status', 'roles', 'actions'];
  protected readonly statuses = USER_STATUSES;
  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly showForm = signal(false);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  private readonly allRoles = signal<Role[]>([]);
  /**
   * Roles que el operador puede otorgar: todos si es SUPER_ADMIN (platformAdmin),
   * o solo los que él mismo posee — así no asigna un privilegio mayor al propio (HU-21 CA1).
   */
  protected readonly assignableRoles = computed<Role[]>(() => {
    const user = this.store.user();
    if (user?.platformAdmin) {
      return this.allRoles();
    }
    const own = new Set(user?.roles ?? []);
    return this.allRoles().filter((r) => own.has(r.code));
  });

  protected readonly searchControl = this.fb.nonNullable.control('');

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    employeeCode: [''],
    password: ['', [Validators.required, Validators.minLength(8)]],
    roleCodes: [[] as string[]],
  });

  constructor() {
    this.roleService.list().subscribe({
      next: (roles) => this.allRoles.set(roles),
      error: () => void 0,
    });
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    const search = this.searchControl.value.trim() || undefined;
    this.service.list(this.page(), this.size(), search).subscribe({
      next: (result) => {
        this.users.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el listado (¿permiso user:manage?).');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected applySearch(): void {
    this.page.set(0);
    this.reload();
  }

  protected create(): void {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    this.loading.set(true);
    this.service
      .create({
        email: raw.email,
        firstName: raw.firstName,
        lastName: raw.lastName,
        employeeCode: raw.employeeCode || undefined,
        password: raw.password,
        roleCodes: raw.roleCodes.length ? raw.roleCodes : undefined,
      })
      .subscribe({
        next: () => {
          this.notify.success('Usuario creado.');
          this.form.reset({ roleCodes: [] });
          this.showForm.set(false);
          this.reload();
        },
        error: () => {
          this.loading.set(false);
          this.notify.error('No se pudo crear el usuario.');
        },
      });
  }

  protected changeStatus(user: User, status: UserStatus): void {
    this.service.updateStatus(user.id, status).subscribe({
      next: () => {
        this.notify.success('Estado actualizado.');
        this.reload();
      },
      error: () => this.notify.error('No se pudo cambiar el estado.'),
    });
  }

  protected openRoles(user: User): void {
    this.dialog
      .open(AssignRolesDialogComponent, {
        data: {
          userLabel: `${user.firstName} ${user.lastName}`,
          assignableRoles: this.assignableRoles(),
          selected: user.roles,
        },
        width: '420px',
      })
      .afterClosed()
      .subscribe((roleCodes: string[] | null | undefined) => {
        if (!roleCodes) {
          return;
        }
        this.service.assignRoles(user.id, roleCodes).subscribe({
          next: () => {
            this.notify.success('Roles actualizados.');
            this.reload();
          },
          error: () => this.notify.error('No se pudieron actualizar los roles.'),
        });
      });
  }
}
