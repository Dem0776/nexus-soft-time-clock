import { Component, inject, signal } from '@angular/core';
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
import { Role } from '../roles/role.models';
import { RoleService } from '../roles/role.service';
import { UserEditDialogComponent } from './user-edit-dialog.component';
import { UserFormDialogComponent } from './user-form-dialog.component';
import { User } from './user.models';
import { UserService } from './user.service';

/** Administración de usuarios del tenant (RF-06, RF-22): alta y edición (perfil, estado, roles) en modal. */
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
                <button mat-stroked-button (click)="openEdit(u)"><mat-icon>edit</mat-icon> Editar</button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns"
                (click)="openEdit(row)" style="cursor:pointer"></tr>
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
  `,
})
export class UsersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['email', 'name', 'employeeCode', 'status', 'roles', 'actions'];
  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

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
