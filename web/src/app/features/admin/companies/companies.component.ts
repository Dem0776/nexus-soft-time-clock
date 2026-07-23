import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';

import { ConfirmDialogComponent } from '../../../core/ui/confirm-dialog.component';
import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { Company } from './company.models';
import { CompanyService } from './company.service';

/** Administración de empresas / tenants (RF-13): listado paginado, alta, edición y cambio de estado. */
@Component({
  selector: 'app-companies',
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
    MatProgressBarModule,
    MatDialogModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Empresas" subtitle="Administra las empresas registradas en el sistema">
      <button mat-flat-button color="primary" (click)="startCreate()">
        <mat-icon>add</mat-icon> Nueva empresa
      </button>
    </app-page-header>

    <div class="split-layout">
      <div class="split-main">
        <mat-card>
          <mat-card-content>
            <div class="filter-bar">
              <mat-form-field appearance="outline" class="search">
                <mat-icon matPrefix>search</mat-icon>
                <mat-label>Buscar por código o nombre</mat-label>
                <input matInput [formControl]="searchControl" (keyup.enter)="applySearch()" />
              </mat-form-field>
              <button mat-stroked-button type="button" (click)="applySearch()">Buscar</button>
            </div>

            @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
            @if (error()) { <p class="error-text">{{ error() }}</p> }

            <div class="table-wrap">
              <table mat-table [dataSource]="companies()" style="width:100%">
                <ng-container matColumnDef="code">
                  <th mat-header-cell *matHeaderCellDef>Código</th>
                  <td mat-cell *matCellDef="let c">{{ c.code }}</td>
                </ng-container>
                <ng-container matColumnDef="name">
                  <th mat-header-cell *matHeaderCellDef>Nombre</th>
                  <td mat-cell *matCellDef="let c">{{ c.name }}</td>
                </ng-container>
                <ng-container matColumnDef="emailDomain">
                  <th mat-header-cell *matHeaderCellDef>Dominio</th>
                  <td mat-cell *matCellDef="let c">{{ c.emailDomain || '—' }}</td>
                </ng-container>
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Estado</th>
                  <td mat-cell *matCellDef="let c"><app-status-chip [status]="c.status" /></td>
                </ng-container>
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef></th>
                  <td mat-cell *matCellDef="let c" style="text-align:right">
                    @if (c.status === 'ACTIVE') {
                      <button mat-icon-button (click)="toggleStatus(c)" aria-label="Suspender"><mat-icon>block</mat-icon></button>
                    } @else {
                      <button mat-icon-button (click)="toggleStatus(c)" aria-label="Activar"><mat-icon>check_circle</mat-icon></button>
                    }
                    <button mat-icon-button (click)="startEdit(c)" aria-label="Editar"><mat-icon>chevron_right</mat-icon></button>
                  </td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="columns"></tr>
                <tr mat-row *matRowDef="let row; columns: columns"
                    (click)="startEdit(row)"
                    [style.background]="row.id === editingId() ? 'var(--brand-soft)' : ''"
                    style="cursor:pointer"></tr>
              </table>
            </div>

            @if (!loading() && companies().length === 0) {
              <app-empty-state icon="business" message="No hay empresas para mostrar." />
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

      @if (showForm()) {
        <aside class="split-drawer">
          <div class="drawer-header">
            <div class="titles">
              <h3>{{ editingId() ? 'Editar empresa' : 'Nueva empresa' }}</h3>
              @if (editingId()) { <p class="sub">Código {{ form.controls.code.value }}</p> }
            </div>
            <button mat-icon-button (click)="cancelForm()" aria-label="Cerrar"><mat-icon>close</mat-icon></button>
          </div>
          <div class="drawer-body">
            <form [formGroup]="form" style="display:flex;flex-direction:column">
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Código</mat-label>
                <input matInput formControlName="code" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Nombre</mat-label>
                <input matInput formControlName="name" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Razón social</mat-label>
                <input matInput formControlName="legalName" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Dominio de correo</mat-label>
                <input matInput formControlName="emailDomain" placeholder="empresa.com" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Zona horaria</mat-label>
                <input matInput formControlName="timezone" placeholder="America/Lima" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Idioma</mat-label>
                <input matInput formControlName="locale" placeholder="es" />
              </mat-form-field>
            </form>
          </div>
          <div class="drawer-actions">
            <button mat-button (click)="cancelForm()">Cancelar</button>
            <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="save()">
              {{ editingId() ? 'Guardar' : 'Crear' }}
            </button>
          </div>
        </aside>
      }
    </div>
  `,
})
export class CompaniesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CompanyService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['code', 'name', 'emailDomain', 'status', 'actions'];
  protected readonly companies = signal<Company[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly showForm = signal(false);
  protected readonly editingId = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly searchControl = this.fb.nonNullable.control('');

  protected readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    name: ['', [Validators.required]],
    legalName: [''],
    emailDomain: [''],
    timezone: [''],
    locale: [''],
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
        this.companies.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el listado (¿permiso company:manage?).');
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

  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.form.controls.code.enable();
    this.showForm.set(true);
  }

  protected startEdit(company: Company): void {
    this.editingId.set(company.id);
    this.form.reset({
      code: company.code,
      name: company.name,
      legalName: company.legalName ?? '',
      emailDomain: company.emailDomain ?? '',
      timezone: company.timezone ?? '',
      locale: company.locale ?? '',
    });
    this.form.controls.code.disable(); // el código es inmutable en edición
    this.showForm.set(true);
  }

  protected cancelForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.form.reset();
    this.form.controls.code.enable();
  }

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    const payload = {
      name: raw.name,
      legalName: raw.legalName || undefined,
      emailDomain: raw.emailDomain || undefined,
      timezone: raw.timezone || undefined,
      locale: raw.locale || undefined,
    };
    this.loading.set(true);
    const editId = this.editingId();
    const request$ = editId
      ? this.service.update(editId, payload)
      : this.service.create({ code: raw.code, ...payload });
    request$.subscribe({
      next: () => {
        this.notify.success(editId ? 'Empresa actualizada.' : 'Empresa creada.');
        this.cancelForm();
        this.reload();
      },
      error: () => {
        this.loading.set(false);
        this.notify.error('No se pudo guardar la empresa.');
      },
    });
  }

  protected toggleStatus(company: Company): void {
    const next = company.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: next === 'SUSPENDED' ? 'Suspender empresa' : 'Activar empresa',
          message: `¿Confirmás cambiar el estado de "${company.name}" a ${next}?`,
          color: next === 'SUSPENDED' ? 'warn' : 'primary',
        },
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.service.setStatus(company.id, next).subscribe({
          next: () => {
            this.notify.success('Estado actualizado.');
            this.reload();
          },
          error: () => this.notify.error('No se pudo cambiar el estado.'),
        });
      });
  }
}
