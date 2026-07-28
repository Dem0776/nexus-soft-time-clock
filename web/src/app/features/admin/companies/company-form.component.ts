import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { BreadcrumbComponent } from '../../../core/ui/breadcrumb.component';
import { ConfirmDialogComponent } from '../../../core/ui/confirm-dialog.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { Company } from './company.models';
import { CompanyService } from './company.service';

/** Alta / edición de empresa en página completa (con breadcrumb), fiel al patrón "Formulario". */
@Component({
  selector: 'app-company-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    BreadcrumbComponent,
    PageHeaderComponent,
    StatusChipComponent,
  ],
  template: `
    <app-breadcrumb
      [items]="[
        { label: 'Organización', route: '/companies' },
        { label: 'Empresas', route: '/companies' },
        { label: isEdit() ? (company()?.name ?? '') : 'Nueva empresa' },
      ]"
    />
    <app-page-header [title]="isEdit() ? 'Editar empresa' : 'Nueva empresa'" />

    @if (loading()) { <mat-progress-bar mode="indeterminate" style="margin-bottom:16px" /> }
    @if (error()) { <p class="error-text">{{ error() }}</p> }

    <div class="form-page-layout">
      <div class="form-page-main">
        <h3 class="form-section-title">Información de la empresa</h3>
        <form [formGroup]="form">
          <div class="form-grid-2">
            <mat-form-field appearance="outline" class="drawer-field">
              <mat-label>Código</mat-label>
              <input matInput formControlName="code" />
              <mat-hint>Código único que identifica a la empresa en el sistema.</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline" class="drawer-field">
              <mat-label>Nombre comercial</mat-label>
              <input matInput formControlName="name" />
              <mat-hint>Nombre con el que se identifica la empresa.</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline" class="drawer-field">
              <mat-label>Razón social</mat-label>
              <input matInput formControlName="legalName" />
              <mat-hint>Nombre legal de la empresa (opcional).</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline" class="drawer-field">
              <mat-label>Dominio de correo</mat-label>
              <input matInput formControlName="emailDomain" placeholder="empresa.com" />
              <mat-hint>Dominio utilizado para accesos y validaciones (opcional).</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline" class="drawer-field">
              <mat-label>Zona horaria</mat-label>
              <input matInput formControlName="timezone" placeholder="America/Lima" />
              <mat-hint>Zona horaria principal de la empresa.</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline" class="drawer-field">
              <mat-label>Idioma</mat-label>
              <input matInput formControlName="locale" placeholder="es" />
              <mat-hint>Idioma y configuración regional.</mat-hint>
            </mat-form-field>
          </div>
        </form>

        <div class="form-callout">
          <mat-icon>info</mat-icon>
          <div>
            <p class="row-title">Importante</p>
            <p class="row-desc">Una vez creada la empresa, podrás administrar sus centros de trabajo, proyectos y horarios.</p>
          </div>
        </div>

        <div class="form-actions">
          <button mat-button type="button" (click)="cancel()">Cancelar</button>
          <button mat-flat-button color="primary" [disabled]="!formValid() || saving()" (click)="save()">
            <mat-icon>save</mat-icon> {{ isEdit() ? 'Guardar cambios' : 'Guardar empresa' }}
          </button>
        </div>
      </div>

      <div class="form-page-side">
        <div class="info-card tip">
          <h3 class="info-card-title">Información importante</h3>
          <div class="icon-row">
            <mat-icon>shield</mat-icon>
            <div>
              <p class="row-title">Código único</p>
              <p class="row-desc">El código de empresa no puede modificarse después de crearla.</p>
            </div>
          </div>
          <div class="icon-row">
            <mat-icon>public</mat-icon>
            <div>
              <p class="row-title">Zona horaria y locale</p>
              <p class="row-desc">Afectan los cálculos de asistencia y reportes.</p>
            </div>
          </div>
          <div class="icon-row">
            <mat-icon>check_circle</mat-icon>
            <div>
              <p class="row-title">Solo empresas activas</p>
              <p class="row-desc">Solo las empresas activas pueden tener usuarios y centros de trabajo.</p>
            </div>
          </div>
        </div>

        @if (isEdit() && company(); as c) {
          <div class="info-card">
            <div class="entity-avatar-row">
              <div class="entity-avatar">{{ initials(c.name) }}</div>
              <div>
                <p class="entity-avatar-name">{{ c.name }}</p>
                <app-status-chip [status]="c.status" />
              </div>
            </div>
            <div class="info-row"><span class="k">Código</span><span class="v">{{ c.code }}</span></div>
          </div>
          <div class="info-card">
            <h3 class="info-card-title">Estado de la empresa</h3>
            <p class="muted" style="margin:0 0 var(--sp-3)">
              @if (c.status === 'ACTIVE') { Solo las empresas activas pueden usar el sistema. }
              @else { Esta empresa no puede operar mientras no esté activa. }
            </p>
            <button mat-stroked-button (click)="toggleStatus(c)">
              {{ c.status === 'ACTIVE' ? 'Suspender empresa' : 'Activar empresa' }}
            </button>
          </div>
        }
      </div>
    </div>
  `,
})
export class CompanyFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(CompanyService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly companyId = this.route.snapshot.paramMap.get('id');
  protected readonly isEdit = computed(() => !!this.companyId);

  protected readonly company = signal<Company | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required]],
    name: ['', [Validators.required]],
    legalName: [''],
    emailDomain: [''],
    timezone: [''],
    locale: [''],
  });

  private readonly formStatus = toSignal(this.form.statusChanges, { initialValue: this.form.status });
  protected readonly formValid = computed(() => this.formStatus() === 'VALID');

  constructor() {
    if (this.companyId) {
      this.loading.set(true);
      this.service.get(this.companyId).subscribe({
        next: (c) => {
          this.company.set(c);
          this.form.reset({
            code: c.code,
            name: c.name,
            legalName: c.legalName ?? '',
            emailDomain: c.emailDomain ?? '',
            timezone: c.timezone ?? '',
            locale: c.locale ?? '',
          });
          this.form.controls.code.disable();
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la empresa.');
          this.loading.set(false);
        },
      });
    }
  }

  protected initials(name: string): string {
    const parts = name.trim().split(/\s+/).slice(0, 2);
    return parts.map((p) => p.charAt(0)).join('').toUpperCase();
  }

  protected cancel(): void {
    void this.router.navigate(['/companies']);
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
    this.saving.set(true);
    const request$ = this.companyId
      ? this.service.update(this.companyId, payload)
      : this.service.create({ code: raw.code, ...payload });
    request$.subscribe({
      next: () => {
        this.notify.success(this.companyId ? 'Empresa actualizada.' : 'Empresa creada.');
        void this.router.navigate(['/companies']);
      },
      error: () => {
        this.saving.set(false);
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
          next: (updated) => {
            this.company.set(updated);
            this.notify.success('Estado actualizado.');
          },
          error: () => this.notify.error('No se pudo cambiar el estado.'),
        });
      });
  }
}
