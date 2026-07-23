import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

import { AuthStore } from '../../../core/auth/auth.store';
import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatCardComponent } from '../../../core/ui/stat-card.component';
import { DashboardSummary } from './dashboard.models';
import { DashboardService } from './dashboard.service';

interface QuickLink {
  route: string;
  icon: string;
  label: string;
  permission?: string;
}

const QUICK_LINKS: readonly QuickLink[] = [
  { route: '/incidents', icon: 'report_problem', label: 'Incidencias', permission: 'incident:approve' },
  { route: '/reports', icon: 'bar_chart', label: 'Reportes', permission: 'report:export' },
  { route: '/map', icon: 'public', label: 'Mapa en tiempo real', permission: 'dashboard:read' },
  { route: '/users', icon: 'group', label: 'Usuarios', permission: 'user:manage' },
  { route: '/work-sites', icon: 'place', label: 'Centros de trabajo', permission: 'worksite:manage' },
  { route: '/scheduling', icon: 'event', label: 'Horarios y turnos', permission: 'schedule:manage' },
];

/**
 * Panel de indicadores (RF-24): stat-cards con la métrica exacta que expone el backend
 * y accesos rápidos a las secciones que el usuario puede operar. Sin gráficas ni
 * comparativos: duplicarían las mismas cinco cifras ya visibles en las tarjetas.
 */
@Component({
  selector: 'app-metrics-dashboard',
  standalone: true,
  imports: [RouterLink, MatIconModule, StatCardComponent, PageHeaderComponent, EmptyStateComponent],
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

      @if (visibleLinks().length > 0) {
        <div class="quick-section">
          <h3 class="section-title">Accesos rápidos</h3>
          <div class="quick-grid">
            @for (link of visibleLinks(); track link.route) {
              <a class="quick-link" [routerLink]="link.route">
                <mat-icon>{{ link.icon }}</mat-icon>
                <span>{{ link.label }}</span>
              </a>
            }
          </div>
        </div>
      }
    } @else if (error()) {
      <app-empty-state icon="error_outline" [message]="error()!" />
    } @else {
      <p class="muted">Cargando indicadores…</p>
    }
  `,
  styles: [
    `
      .stat-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
        gap: var(--sp-4);
      }
      .section-title {
        font-size: var(--font-small);
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.04em;
        color: var(--text-muted);
        margin: var(--sp-6) 0 var(--sp-3);
      }
      .quick-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: var(--sp-3);
      }
      .quick-link {
        display: flex;
        align-items: center;
        gap: var(--sp-3);
        padding: var(--sp-4);
        background: var(--surface);
        border: 1px solid var(--border);
        border-radius: var(--radius-lg);
        color: var(--text);
        text-decoration: none;
        font-weight: 600;
        font-size: var(--font-body);
        transition: border-color 0.12s ease, background 0.12s ease;
      }
      .quick-link:hover { background: var(--surface-hover); border-color: var(--brand-border); }
      .quick-link mat-icon { color: var(--brand); }
    `,
  ],
})
export class MetricsDashboardComponent {
  private readonly service = inject(DashboardService);
  private readonly store = inject(AuthStore);

  protected readonly summary = signal<DashboardSummary | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly visibleLinks = computed(() =>
    QUICK_LINKS.filter((l) => !l.permission || this.store.hasPermission(l.permission)),
  );

  constructor() {
    this.service.summary().subscribe({
      next: (s) => this.summary.set(s),
      error: () => this.error.set('No se pudieron cargar los indicadores (¿permiso dashboard:read?).'),
    });
  }
}
