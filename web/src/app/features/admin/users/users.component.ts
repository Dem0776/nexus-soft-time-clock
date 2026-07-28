import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { Role } from '../roles/role.models';
import { RoleService } from '../roles/role.service';
import { UserFormDialogComponent } from './user-form-dialog.component';
import { USER_STATUSES, User, UserStatus } from './user.models';
import { UserService } from './user.service';

/** Administración de usuarios del tenant (RF-06, RF-22): alta (con perfil), listado, estado y roles. */
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
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatProgressBarModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Usuarios" subtitle="Administra las personas con acceso al sistema y su información laboral">
      <button mat-flat-button color="primary" (click)="openCreate()">
        <mat-icon>person_add</mat-icon> Nuevo usuario
      </button>
    </app-page-header>

    <div class="split-layout">
      <div class="split-main">
        <mat-card>
          <mat-card-content>
            <div class="filter-bar">
              <mat-form-field appearance="outline" class="search">
                <mat-icon matPrefix>search</mat-icon>
                <mat-label>Buscar por nombre o correo</mat-label>
                <input matInput [formControl]="searchControl" (keyup.enter)="applySearch()" />
              </mat-form-field>
              <button mat-stroked-button type="button" (click)="applySearch()">Buscar</button>
            </div>

            @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
            @if (error()) { <p class="error-text">{{ error() }}</p> }

            <div class="table-wrap">
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
                    <button mat-icon-button (click)="openDetail(u)" aria-label="Detalle"><mat-icon>chevron_right</mat-icon></button>
                  </td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="columns"></tr>
                <tr mat-row *matRowDef="let row; columns: columns"
                    (click)="openDetail(row)"
                    [style.background]="row.id === selectedUser()?.id ? 'var(--brand-soft)' : ''"
                    style="cursor:pointer"></tr>
              </table>
            </div>

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
      </div>

      @if (selectedUser(); as u) {
        <aside class="split-drawer">
          <div class="drawer-header">
            <div class="titles"><h3>{{ u.firstName }} {{ u.lastName }}</h3><p class="sub">{{ u.email }}</p></div>
            <button mat-icon-button (click)="closeDrawer()" aria-label="Cerrar"><mat-icon>close</mat-icon></button>
          </div>
          <div class="drawer-body">
            <div class="detail-section-title">Información</div>
            <div class="detail-grid">
              <div class="detail-row"><span class="k">Código de empleado</span><span class="v">{{ u.employeeCode || '—' }}</span></div>
              <div class="detail-row"><span class="k">Estado</span><span class="v"><app-status-chip [status]="u.status" /></span></div>
            </div>

            <div class="detail-section-title">Cambiar estado</div>
            <div class="status-actions">
              @for (s of statuses; track s) {
                <button mat-stroked-button [disabled]="s === u.status" (click)="changeStatus(u, s)">{{ s }}</button>
              }
            </div>

            <div class="detail-section-title">Roles</div>
            @if (assignableRoles().length === 0) {
              <p class="muted">No tenés roles asignables (no podés otorgar roles de mayor privilegio que el propio).</p>
            }
            <div class="roles-list">
              @for (role of assignableRoles(); track role.code) {
                <mat-checkbox [checked]="isRoleSelected(role.code)" (change)="toggleRole(role.code, $event.checked)">
                  {{ role.name }} <span class="muted">({{ role.code }})</span>
                </mat-checkbox>
              }
            </div>
          </div>
          <div class="drawer-actions">
            <button mat-flat-button color="primary" [disabled]="pendingRoles().length === 0" (click)="saveRoles(u)">
              Guardar roles
            </button>
          </div>
        </aside>
      }
    </div>
  `,
  styles: [
    `
      .status-actions { display: flex; gap: var(--sp-2); flex-wrap: wrap; margin-bottom: var(--sp-2); }
      .roles-list { display: flex; flex-direction: column; gap: var(--sp-2); }
    `,
  ],
})
export class UsersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['email', 'name', 'employeeCode', 'status', 'roles', 'actions'];
  protected readonly statuses = USER_STATUSES;
  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly selectedUser = signal<User | null>(null);
  protected readonly pendingRoles = signal<string[]>([]);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  /** Roles que el operador puede otorgar (el backend ya filtra por potestad de delegación, HU-21). */
  protected readonly assignableRoles = signal<Role[]>([]);

  protected readonly searchControl = this.fb.nonNullable.control('');

  constructor() {
    this.roleService.list().subscribe({
      next: (roles) => this.assignableRoles.set(roles),
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

  protected openCreate(): void {
    const ref = this.dialog.open(UserFormDialogComponent, {
      width: '760px',
      maxWidth: '96vw',
      autoFocus: false,
      data: { assignableRoles: this.assignableRoles() },
    });
    ref.afterClosed().subscribe((created) => {
      if (created) this.reload();
    });
  }

  protected closeDrawer(): void {
    this.selectedUser.set(null);
  }

  protected changeStatus(user: User, status: UserStatus): void {
    this.service.updateStatus(user.id, status).subscribe({
      next: () => {
        this.notify.success('Estado actualizado.');
        this.selectedUser.set({ ...user, status });
        this.reload();
      },
      error: () => this.notify.error('No se pudo cambiar el estado.'),
    });
  }

  protected openDetail(user: User): void {
    this.selectedUser.set(user);
    this.pendingRoles.set([...user.roles]);
  }

  protected isRoleSelected(code: string): boolean {
    return this.pendingRoles().includes(code);
  }

  protected toggleRole(code: string, checked: boolean): void {
    this.pendingRoles.update((list) =>
      checked ? [...new Set([...list, code])] : list.filter((c) => c !== code),
    );
  }

  protected saveRoles(user: User): void {
    this.service.assignRoles(user.id, this.pendingRoles()).subscribe({
      next: () => {
        this.notify.success('Roles actualizados.');
        this.reload();
        this.closeDrawer();
      },
      error: () => this.notify.error('No se pudieron actualizar los roles.'),
    });
  }
}
