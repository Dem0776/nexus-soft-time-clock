import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { VacationPolicy } from './vacation-policy.models';
import { VacationPolicyService } from './vacation-policy.service';

/**
 * Configuración de la empresa. En esta iteración: política de vacaciones (días/año,
 * aprobación, conteo de días hábiles). Requiere {@code vacation:manage}.
 */
@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatProgressBarModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header title="Configuración" subtitle="Parámetros generales de la empresa. Aplican a todo el tenant." />
    @if (error()) { <p class="error-text">{{ error() }}</p> }
    @if (loading()) { <mat-progress-bar mode="indeterminate" /> }

    <div class="cfg-grid">
      <mat-card>
        <mat-card-content>
          <div class="card-head">
            <mat-icon>beach_access</mat-icon>
            <h3>Vacaciones</h3>
          </div>
          <p class="muted sub">Reglas con las que se otorgan y aprueban las vacaciones.</p>

          <div class="cfg-item">
            <div class="ci-ic"><mat-icon>event_available</mat-icon></div>
            <div class="ci-txt">
              <h4>Días de vacaciones por año</h4>
              <p>Días base que se otorgan a cada colaborador por cada año trabajado.</p>
            </div>
            <div class="stepper">
              <button mat-icon-button type="button" (click)="adjust(-1)" [disabled]="model.daysPerYear <= 0"><mat-icon>remove</mat-icon></button>
              <input type="number" min="0" max="366" [(ngModel)]="model.daysPerYear" />
              <button mat-icon-button type="button" (click)="adjust(1)" [disabled]="model.daysPerYear >= 366"><mat-icon>add</mat-icon></button>
            </div>
          </div>

          <div class="cfg-item">
            <div class="ci-ic"><mat-icon>verified_user</mat-icon></div>
            <div class="ci-txt">
              <h4>Requiere aprobación de supervisor</h4>
              <p>Si se desactiva, las solicitudes se aprueban automáticamente.</p>
            </div>
            <mat-slide-toggle [(ngModel)]="model.requireApproval" color="primary" />
          </div>

          <div class="cfg-item">
            <div class="ci-ic"><mat-icon>calendar_month</mat-icon></div>
            <div class="ci-txt">
              <h4>Contar solo días hábiles</h4>
              <p>Excluye sábados y domingos del conteo de días solicitados.</p>
            </div>
            <mat-slide-toggle [(ngModel)]="model.countBusinessDaysOnly" color="primary" />
          </div>

          <div class="actions">
            <button mat-button type="button" [disabled]="saving()" (click)="load()">Descartar</button>
            <button mat-flat-button color="primary" [disabled]="saving()" (click)="save()">
              <mat-icon>save</mat-icon> Guardar cambios
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-content>
          <div class="info-box">
            <mat-icon>info</mat-icon>
            <div>
              Con <b>{{ model.daysPerYear }} días/año</b>, un colaborador con <b>1 año</b> de antigüedad acumula
              <b>{{ model.daysPerYear }} días</b>; con 2 años, {{ model.daysPerYear * 2 }}, y así sucesivamente. La
              <b>fecha de ingreso</b> de cada persona (en su ficha) determina la antigüedad.
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .cfg-grid { display: grid; grid-template-columns: 1.3fr 1fr; gap: var(--sp-4); align-items: start; }
      @media (max-width: 1000px) { .cfg-grid { grid-template-columns: 1fr; } }
      .card-head { display: flex; align-items: center; gap: 10px; }
      .card-head mat-icon { color: var(--brand); }
      .card-head h3 { margin: 0; font-size: 1.05rem; font-weight: 700; }
      .sub { margin: 4px 0 8px; font-size: var(--font-small); }
      .cfg-item { display: flex; align-items: center; gap: var(--sp-4); padding: var(--sp-4) 0; border-top: 1px solid var(--border); }
      .cfg-item:first-of-type { border-top: none; }
      .ci-ic { width: 40px; height: 40px; border-radius: 11px; background: var(--brand-soft); border: 1px solid var(--brand-border); color: var(--brand); display: grid; place-items: center; flex: none; }
      .ci-txt { flex: 1; }
      .ci-txt h4 { margin: 0; font-size: var(--font-body); font-weight: 600; }
      .ci-txt p { margin: 3px 0 0; font-size: var(--font-small); color: var(--text-muted); }
      .stepper { display: inline-flex; align-items: center; border: 1px solid var(--border-strong); border-radius: 10px; overflow: hidden; }
      .stepper input { width: 56px; text-align: center; border: none; outline: none; background: transparent; color: var(--text); font: inherit; font-weight: 700; font-size: 1rem; }
      .actions { margin-top: var(--sp-4); display: flex; justify-content: flex-end; gap: var(--sp-2); }
      .info-box { display: flex; gap: 11px; padding: 4px; color: var(--text); font-size: var(--font-small); line-height: 1.5; }
      .info-box mat-icon { color: var(--info); flex: none; }
    `,
  ],
})
export class SettingsComponent {
  private readonly service = inject(VacationPolicyService);
  private readonly notify = inject(NotificationService);

  protected model: VacationPolicy = { daysPerYear: 12, requireApproval: true, countBusinessDaysOnly: true };
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.get().subscribe({
      next: (policy) => {
        this.model = { ...policy };
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la configuración de vacaciones.');
        this.loading.set(false);
      },
    });
  }

  adjust(delta: number): void {
    this.model.daysPerYear = Math.min(366, Math.max(0, (this.model.daysPerYear ?? 0) + delta));
  }

  save(): void {
    this.saving.set(true);
    this.service.update(this.model).subscribe({
      next: (policy) => {
        this.model = { ...policy };
        this.saving.set(false);
        this.notify.success('Configuración guardada.');
      },
      error: () => {
        this.saving.set(false);
        this.notify.error('No se pudieron guardar los cambios.');
      },
    });
  }
}
