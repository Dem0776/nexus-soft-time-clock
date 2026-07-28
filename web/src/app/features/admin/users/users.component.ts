import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { USER_STATUSES, User } from './user.models';
import { UserService } from './user.service';

/** Catálogo de usuarios del tenant (RF-06). Alta y edición viven en páginas propias con breadcrumb. */
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
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressBarModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Usuarios" subtitle="Administra los usuarios que tienen acceso al sistema">
      <button mat-flat-button color="primary" (click)="router.navigate(['/users/new'])">
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
          <mat-form-field appearance="outline" style="width:170px">
            <mat-label>Estado</mat-label>
            <mat-select [formControl]="statusFilter">
              <mat-option value="">Todos</mat-option>
              @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" style="width:170px">
            <mat-label>Rol</mat-label>
            <mat-select [formControl]="roleFilter">
              <mat-option value="">Todos</mat-option>
              @for (r of roleOptions(); track r) { <mat-option [value]="r">{{ r }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <button mat-stroked-button type="button" (click)="applySearch()">Buscar</button>
        </div>

        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <div class="table-wrap">
          <table mat-table [dataSource]="filtered()" style="width:100%">
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
                <button mat-icon-button (click)="edit(u)" aria-label="Editar"><mat-icon>chevron_right</mat-icon></button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns" (click)="edit(row)" style="cursor:pointer"></tr>
          </table>
        </div>

        @if (!loading() && filtered().length === 0) {
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
  protected readonly router = inject(Router);

  protected readonly columns = ['email', 'name', 'employeeCode', 'status', 'roles', 'actions'];
  protected readonly statuses = USER_STATUSES;
  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly searchControl = this.fb.nonNullable.control('');
  protected readonly statusFilter = this.fb.nonNullable.control('');
  protected readonly roleFilter = this.fb.nonNullable.control('');

  private readonly statusFilterValue = toSignal(this.statusFilter.valueChanges, { initialValue: '' });
  private readonly roleFilterValue = toSignal(this.roleFilter.valueChanges, { initialValue: '' });

  protected readonly roleOptions = computed(() => [...new Set(this.users().flatMap((u) => u.roles))].sort());

  protected readonly filtered = computed(() => {
    const status = this.statusFilterValue();
    const role = this.roleFilterValue();
    return this.users().filter((u) => {
      if (status && u.status !== status) return false;
      if (role && !u.roles.includes(role)) return false;
      return true;
    });
  });

  constructor() {
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

  protected edit(user: User): void {
    void this.router.navigate(['/users', user.id, 'edit']);
  }
}
