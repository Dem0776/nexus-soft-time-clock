import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';

import { BreadcrumbComponent } from '../../../core/ui/breadcrumb.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { PROJECT_STATUSES, Project } from './project.models';
import { ProjectService } from './project.service';

/** Alta / edición de proyecto en página completa (con breadcrumb). */
@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
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
        { label: 'Planificación', route: '/projects' },
        { label: 'Proyectos', route: '/projects' },
        { label: isEdit() ? (project()?.name ?? '') : 'Nuevo proyecto' },
      ]"
    />
    <app-page-header [title]="isEdit() ? 'Editar proyecto' : 'Nuevo proyecto'" />

    @if (loading()) { <mat-progress-bar mode="indeterminate" style="margin-bottom:16px" /> }
    @if (error()) { <p class="error-text">{{ error() }}</p> }

    <div class="form-page-layout">
      <div class="form-page-main">
        <form [formGroup]="form">
          <div class="form-section">
            <h3 class="form-section-title">Información del proyecto</h3>
            <div class="form-grid-2">
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Código</mat-label>
                <input matInput formControlName="code" />
                <mat-hint>Código único que identifica al proyecto.</mat-hint>
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Nombre</mat-label>
                <input matInput formControlName="name" />
                <mat-hint>Nombre con el que se identifica el proyecto.</mat-hint>
              </mat-form-field>
              @if (isEdit()) {
                <mat-form-field appearance="outline" class="drawer-field">
                  <mat-label>Estado</mat-label>
                  <mat-select formControlName="status">
                    @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
                  </mat-select>
                  <mat-hint>Solo los proyectos activos pueden utilizarse para asignaciones.</mat-hint>
                </mat-form-field>
              }
            </div>
          </div>

          <div class="form-section">
            <h3 class="form-section-title">Fechas del proyecto (opcional)</h3>
            <div class="form-grid-2">
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Inicio</mat-label>
                <input matInput type="date" formControlName="startsOn" />
                <mat-hint>Fecha en que inicia el proyecto.</mat-hint>
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Fin</mat-label>
                <input matInput type="date" formControlName="endsOn" />
                <mat-hint>Fecha estimada de finalización del proyecto.</mat-hint>
              </mat-form-field>
            </div>
          </div>
        </form>

        <div class="form-actions">
          <button mat-button type="button" (click)="cancel()">Cancelar</button>
          <button mat-flat-button color="primary" [disabled]="!formValid() || saving()" (click)="save()">
            <mat-icon>save</mat-icon> {{ isEdit() ? 'Guardar cambios' : 'Guardar proyecto' }}
          </button>
        </div>
      </div>

      <div class="form-page-side">
        <div class="info-card tip">
          <h3 class="info-card-title">Información importante</h3>
          <div class="icon-row">
            <mat-icon>shield</mat-icon>
            <div>
              <p class="row-title">¿Qué es un proyecto?</p>
              <p class="row-desc">Un proyecto agrupa horarios y asignaciones para una obra o iniciativa específica dentro de tu organización.</p>
            </div>
          </div>
          <div class="icon-row">
            <mat-icon>badge</mat-icon>
            <div>
              <p class="row-title">Código único</p>
              <p class="row-desc">El código de proyecto no puede modificarse después de crearlo.</p>
            </div>
          </div>
        </div>

        @if (isEdit() && project(); as p) {
          <div class="info-card">
            <div class="entity-avatar-row">
              <div class="entity-avatar"><mat-icon>folder</mat-icon></div>
              <div>
                <p class="entity-avatar-name">{{ p.name }}</p>
                <app-status-chip [status]="p.status" />
              </div>
            </div>
            <div class="info-row"><span class="k">Código</span><span class="v">{{ p.code }}</span></div>
          </div>
        }
      </div>
    </div>
  `,
})
export class ProjectFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ProjectService);
  private readonly notify = inject(NotificationService);

  protected readonly statuses = PROJECT_STATUSES;
  protected readonly projectId = this.route.snapshot.paramMap.get('id');
  protected readonly isEdit = computed(() => !!this.projectId);

  protected readonly project = signal<Project | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    name: ['', [Validators.required]],
    status: ['ACTIVE'],
    startsOn: [''],
    endsOn: [''],
  });

  private readonly formStatus = toSignal(this.form.statusChanges, { initialValue: this.form.status });
  protected readonly formValid = computed(() => this.formStatus() === 'VALID');

  constructor() {
    if (this.projectId) {
      this.loading.set(true);
      this.service.get(this.projectId).subscribe({
        next: (p) => {
          this.project.set(p);
          this.form.reset({
            code: p.code,
            name: p.name,
            status: p.status,
            startsOn: p.startsOn ?? '',
            endsOn: p.endsOn ?? '',
          });
          this.form.controls.code.disable();
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el proyecto.');
          this.loading.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    void this.router.navigate(['/projects']);
  }

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    this.saving.set(true);
    const request$ = this.projectId
      ? this.service.update(this.projectId, {
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
        this.notify.success(this.projectId ? 'Proyecto actualizado.' : 'Proyecto creado.');
        void this.router.navigate(['/projects']);
      },
      error: () => {
        this.saving.set(false);
        this.notify.error('No se pudo guardar el proyecto.');
      },
    });
  }
}
