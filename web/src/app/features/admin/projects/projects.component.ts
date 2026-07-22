import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { PROJECT_STATUSES, Project } from './project.models';
import { ProjectService } from './project.service';

/** Administración de proyectos (RF-23): listado, alta y edición. */
@Component({
  selector: 'app-projects',
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
    <app-page-header title="Proyectos">
      <button mat-flat-button color="primary" (click)="startCreate()">
        <mat-icon>add</mat-icon> Nuevo proyecto
      </button>
    </app-page-header>

    @if (showForm()) {
      <mat-card style="margin-bottom:16px">
        <mat-card-header>
          <mat-card-title>{{ editingId() ? 'Editar proyecto' : 'Nuevo proyecto' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="save()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:baseline">
            <mat-form-field appearance="outline">
              <mat-label>Código</mat-label>
              <input matInput formControlName="code" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Nombre</mat-label>
              <input matInput formControlName="name" />
            </mat-form-field>
            @if (editingId()) {
              <mat-form-field appearance="outline" style="width:150px">
                <mat-label>Estado</mat-label>
                <mat-select formControlName="status">
                  @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
                </mat-select>
              </mat-form-field>
            }
            <mat-form-field appearance="outline" style="width:160px">
              <mat-label>Inicio</mat-label>
              <input matInput type="date" formControlName="startsOn" />
            </mat-form-field>
            <mat-form-field appearance="outline" style="width:160px">
              <mat-label>Fin</mat-label>
              <input matInput type="date" formControlName="endsOn" />
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || loading()">
              {{ editingId() ? 'Guardar' : 'Crear' }}
            </button>
            <button mat-button type="button" (click)="cancelForm()">Cancelar</button>
          </form>
        </mat-card-content>
      </mat-card>
    }

    <mat-card>
      <mat-card-content>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <table mat-table [dataSource]="projects()" style="width:100%">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Código</th>
            <td mat-cell *matCellDef="let p">{{ p.code }}</td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let p">{{ p.name }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Estado</th>
            <td mat-cell *matCellDef="let p"><app-status-chip [status]="p.status" /></td>
          </ng-container>
          <ng-container matColumnDef="dates">
            <th mat-header-cell *matHeaderCellDef>Vigencia</th>
            <td mat-cell *matCellDef="let p">{{ p.startsOn || '—' }} → {{ p.endsOn || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let p" style="text-align:right">
              <button mat-icon-button (click)="startEdit(p)" aria-label="Editar"><mat-icon>edit</mat-icon></button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>

        @if (!loading() && projects().length === 0) {
          <app-empty-state icon="work" message="No hay proyectos para mostrar." />
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
export class ProjectsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProjectService);
  private readonly notify = inject(NotificationService);

  protected readonly columns = ['code', 'name', 'status', 'dates', 'actions'];
  protected readonly statuses = PROJECT_STATUSES;
  protected readonly projects = signal<Project[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly showForm = signal(false);
  protected readonly editingId = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    name: ['', [Validators.required]],
    status: ['ACTIVE'],
    startsOn: [''],
    endsOn: [''],
  });

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(this.page(), this.size()).subscribe({
      next: (result) => {
        this.projects.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el listado (¿permiso project:manage?).');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset({ status: 'ACTIVE' });
    this.form.controls.code.enable();
    this.showForm.set(true);
  }

  protected startEdit(project: Project): void {
    this.editingId.set(project.id);
    this.form.reset({
      code: project.code,
      name: project.name,
      status: project.status,
      startsOn: project.startsOn ?? '',
      endsOn: project.endsOn ?? '',
    });
    this.form.controls.code.disable();
    this.showForm.set(true);
  }

  protected cancelForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.form.controls.code.enable();
    this.form.reset({ status: 'ACTIVE' });
  }

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    this.loading.set(true);
    const editId = this.editingId();
    const request$ = editId
      ? this.service.update(editId, {
          name: raw.name,
          status: raw.status as Project['status'],
          startsOn: raw.startsOn || undefined,
          endsOn: raw.endsOn || undefined,
        })
      : this.service.create({
          code: raw.code,
          name: raw.name,
          startsOn: raw.startsOn || undefined,
          endsOn: raw.endsOn || undefined,
        });
    request$.subscribe({
      next: () => {
        this.notify.success(editId ? 'Proyecto actualizado.' : 'Proyecto creado.');
        this.cancelForm();
        this.reload();
      },
      error: () => {
        this.loading.set(false);
        this.notify.error('No se pudo guardar el proyecto.');
      },
    });
  }
}
