import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { ROLE_OPTIONS, Role, roleLabel } from '../roles/role.models';
import { RoleService } from '../roles/role.service';
import { UserEditDialogComponent } from './user-edit-dialog.component';
import { UserFormDialogComponent } from './user-form-dialog.component';
import { USER_STATUSES, User, UserStatus } from './user.models';
import { UserService } from './user.service';

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
  LOCKED: 'Bloqueado',
  INVITED: 'Invitado',
};

/** Administración de usuarios del tenant (RF-06, RF-22): alta/edición en modal, roles en español y filtros por columna. */
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
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
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

    <mat-card>
      <mat-card-content>
        <div class="filter-bar">
          <mat-form-field appearance="outline" class="search">
            <mat-icon matPrefix>search</mat-icon>
            <mat-label>Buscar por nombre o correo</mat-label>
            <input matInput [formControl]="searchControl" (keyup.enter)="applySearch()" />
          </mat-form-field>
          <button mat-stroked-button type="button" (click)="applySearch()">Buscar</button>
          @if (hasColumnFilters()) {
            <button mat-button type="button" (click)="clearColumnFilters()"><mat-icon>filter_alt_off</mat-icon> Limpiar filtros</button>
          }
        </div>

        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <div class="table-wrap">
          <table mat-table [dataSource]="filtered()" style="width:100%">
            <!-- Columnas de datos -->
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
              <td mat-cell *matCellDef="let u">
                @if (u.roles.length) {
                  <span class="role-tags">
                    @for (r of u.roles; track r) { <span class="role-tag">{{ label(r) }}</span> }
                  </span>
                } @else { — }
              </td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let u" style="text-align:right;white-space:nowrap">
                <button mat-stroked-button (click)="openEdit(u); $event.stopPropagation()"><mat-icon>edit</mat-icon> Editar</button>
              </td>
            </ng-container>

            <!-- Fila de filtros por columna (estilo Excel) -->
            <ng-container matColumnDef="email-f">
              <th mat-header-cell *matHeaderCellDef class="fcell">
                <input class="col-filter" placeholder="Filtrar…" [value]="fEmail()" (input)="fEmail.set(asValue($event))" />
              </th>
            </ng-container>
            <ng-container matColumnDef="name-f">
              <th mat-header-cell *matHeaderCellDef class="fcell">
                <input class="col-filter" placeholder="Filtrar…" [value]="fName()" (input)="fName.set(asValue($event))" />
              </th>
            </ng-container>
            <ng-container matColumnDef="employeeCode-f">
              <th mat-header-cell *matHeaderCellDef class="fcell">
                <input class="col-filter" placeholder="Filtrar…" [value]="fCode()" (input)="fCode.set(asValue($event))" />
              </th>
            </ng-container>
            <ng-container matColumnDef="status-f">
              <th mat-header-cell *matHeaderCellDef class="fcell">
                <select class="col-filter" [value]="fStatus()" (change)="fStatus.set(asValue($event))">
                  <option value="">Todos</option>
                  @for (s of statuses; track s) { <option [value]="s">{{ statusLabel(s) }}</option> }
                </select>
              </th>
            </ng-container>
            <ng-container matColumnDef="roles-f">
              <th mat-header-cell *matHeaderCellDef class="fcell">
                <select class="col-filter" [value]="fRole()" (change)="fRole.set(asValue($event))">
                  <option value="">Todos</option>
                  @for (r of roleOptions; track r.code) { <option [value]="r.code">{{ r.label }}</option> }
                </select>
              </th>
            </ng-container>
            <ng-container matColumnDef="actions-f">
              <th mat-header-cell *matHeaderCellDef class="fcell"></th>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-header-row *matHeaderRowDef="filterColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns" (click)="openEdit(row)" style="cursor:pointer"></tr>
          </table>
        </div>

        @if (!loading() && filtered().length === 0) {
          <app-empty-state icon="group" message="No hay usuarios que coincidan." />
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
  styles: [
    `
      .role-tags { display: inline-flex; flex-wrap: wrap; gap: 6px; }
      .role-tag { background: var(--surface-2); border: 1px solid var(--border); color: var(--text-muted); border-radius: 999px; padding: 2px 10px; font-size: var(--font-small); font-weight: 600; white-space: nowrap; }
      .fcell { padding-top: 6px !important; padding-bottom: 6px !important; }
      .col-filter { width: 100%; min-width: 90px; padding: 6px 8px; border: 1px solid var(--border-strong); border-radius: 8px; background: var(--surface); color: var(--text); font: inherit; font-size: var(--font-small); }
      .col-filter:focus { outline: none; border-color: var(--brand); box-shadow: 0 0 0 3px var(--brand-soft); }
      select.col-filter { cursor: pointer; }
    `,
  ],
})
export class UsersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['email', 'name', 'employeeCode', 'status', 'roles', 'actions'];
  protected readonly filterColumns = ['email-f', 'name-f', 'employeeCode-f', 'status-f', 'roles-f', 'actions-f'];
  protected readonly statuses = USER_STATUSES;
  protected readonly roleOptions = ROLE_OPTIONS;

  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly assignableRoles = signal<Role[]>([]);
  protected readonly searchControl = this.fb.nonNullable.control('');

  // Filtros por columna (sobre la página cargada)
  protected readonly fEmail = signal('');
  protected readonly fName = signal('');
  protected readonly fCode = signal('');
  protected readonly fStatus = signal<string>('');
  protected readonly fRole = signal<string>('');

  protected readonly hasColumnFilters = computed(
    () => !!(this.fEmail() || this.fName() || this.fCode() || this.fStatus() || this.fRole()),
  );

  protected readonly filtered = computed(() => {
    const email = this.fEmail().trim().toLowerCase();
    const name = this.fName().trim().toLowerCase();
    const code = this.fCode().trim().toLowerCase();
    const status = this.fStatus();
    const role = this.fRole();
    return this.users().filter(
      (u) =>
        (!email || u.email.toLowerCase().includes(email)) &&
        (!name || `${u.firstName} ${u.lastName}`.toLowerCase().includes(name)) &&
        (!code || (u.employeeCode ?? '').toLowerCase().includes(code)) &&
        (!status || u.status === status) &&
        (!role || u.roles.includes(role)),
    );
  });

  constructor() {
    this.roleService.list().subscribe({
      next: (roles) => this.assignableRoles.set(roles),
      error: () => void 0,
    });
    this.reload();
  }

  protected label(code: string): string {
    return roleLabel(code);
  }

  protected statusLabel(code: string): string {
    return STATUS_LABELS[code] ?? code;
  }

  protected asValue(event: Event): string {
    return (event.target as HTMLInputElement | HTMLSelectElement).value;
  }

  protected clearColumnFilters(): void {
    this.fEmail.set('');
    this.fName.set('');
    this.fCode.set('');
    this.fStatus.set('');
    this.fRole.set('');
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

  protected openEdit(user: User): void {
    const ref = this.dialog.open(UserEditDialogComponent, {
      width: '720px',
      maxWidth: '96vw',
      autoFocus: false,
      data: { user, assignableRoles: this.assignableRoles() },
    });
    ref.afterClosed().subscribe((changed) => {
      if (changed) this.reload();
    });
  }
}
