import { Component, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatCardComponent } from '../../../core/ui/stat-card.component';
import { DashboardSummary } from './dashboard.models';
import { DashboardService } from './dashboard.service';

interface Bar {
  label: string;
  value: number;
  color: string;
}

/**
 * Panel de indicadores (RF-24): stat-cards con ícono/color por métrica y un gráfico de
 * barras (SVG inline, sin dependencias). El mapa en tiempo real vive en su propia vista.
 */
@Component({
  selector: 'app-metrics-dashboard',
  standalone: true,
  imports: [MatCardModule, StatCardComponent, PageHeaderComponent, EmptyStateComponent],
  template: `
    <app-page-header title="Panel" subtitle="Resumen operativo de tu empresa" />

    @if (summary(); as s) {
      <div class="stat-grid">
        <app-stat-card icon="task_alt" tone="success" [value]="s.attendanceTodayAccepted" label="Asistencias hoy" />
        <app-stat-card icon="cancel" tone="danger" [value]="s.attendanceTodayRejected" label="Rechazos hoy" />
        <app-stat-card icon="report_problem" tone="warning" [value]="s.openIncidents" label="Incidencias abiertas" />
        <app-stat-card icon="group" tone="info" [value]="s.activeUsers" label="Usuarios activos" />
        <app-stat-card icon="place" tone="neutral" [value]="s.activeWorkSites" label="Centros activos" />
      </div>

      <mat-card style="margin-top:20px">
        <mat-card-header><mat-card-title>Comparativo de indicadores</mat-card-title></mat-card-header>
        <mat-card-content>
          <svg [attr.viewBox]="'0 0 100 ' + bars().length * 12" style="width:100%;max-width:720px" role="img" aria-label="Gráfico de indicadores">
            <line x1="28" y1="0" x2="28" [attr.y2]="bars().length * 12" stroke="var(--border)" stroke-width="0.3" />
            @for (bar of bars(); track bar.label; let i = $index) {
              <rect x="28" [attr.y]="i * 12 + 2.5" [attr.width]="width(bar.value)" height="7" [attr.fill]="bar.color" rx="1.5" />
              <text x="0" [attr.y]="i * 12 + 8" font-size="3.6" fill="var(--text-muted)">{{ bar.label }}</text>
              <text [attr.x]="width(bar.value) + 30" [attr.y]="i * 12 + 8" font-size="3.6" fill="var(--text)" font-weight="600">{{ bar.value }}</text>
            }
          </svg>
        </mat-card-content>
      </mat-card>
    } @else if (error()) {
      <mat-card><mat-card-content>
        <app-empty-state icon="error_outline" [message]="error()!" />
      </mat-card-content></mat-card>
    } @else {
      <p class="muted">Cargando indicadores…</p>
    }
  `,
  styles: [
    `
      .stat-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
        gap: 16px;
      }
    `,
  ],
})
export class MetricsDashboardComponent {
  private readonly service = inject(DashboardService);

  protected readonly summary = signal<DashboardSummary | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly bars = computed<Bar[]>(() => {
    const s = this.summary();
    if (!s) {
      return [];
    }
    return [
      { label: 'Asist.', value: s.attendanceTodayAccepted, color: 'var(--success)' },
      { label: 'Rech.', value: s.attendanceTodayRejected, color: 'var(--danger)' },
      { label: 'Incid.', value: s.openIncidents, color: 'var(--warning)' },
      { label: 'Usu.', value: s.activeUsers, color: 'var(--info)' },
      { label: 'Centros', value: s.activeWorkSites, color: 'var(--neutral)' },
    ];
  });

  private readonly max = computed(() => Math.max(1, ...this.bars().map((b) => b.value)));

  constructor() {
    this.service.summary().subscribe({
      next: (s) => this.summary.set(s),
      error: () => this.error.set('No se pudieron cargar los indicadores (¿permiso dashboard:read?).'),
    });
  }

  protected width(value: number): number {
    return (value / this.max()) * 60;
  }
}
