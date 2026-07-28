import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { AuthStore } from '../../../core/auth/auth.store';
import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { DashboardSummary } from './dashboard.models';
import { DashboardService } from './dashboard.service';

interface QuickLink {
  route: string;
  icon: string;
  label: string;
  permission?: string;
}

const QUICK_LINKS: readonly QuickLink[] = [
  { route: '/incidents', icon: 'report_problem', label: 'Nueva incidencia', permission: 'incident:approve' },
  { route: '/reports', icon: 'bar_chart', label: 'Generar reporte', permission: 'report:export' },
  { route: '/users', icon: 'group', label: 'Usuarios', permission: 'user:manage' },
  { route: '/work-sites', icon: 'place', label: 'Centros de trabajo', permission: 'worksite:manage' },
  { route: '/map', icon: 'public', label: 'Ver mapa en vivo', permission: 'dashboard:read' },
  { route: '/scheduling', icon: 'event', label: 'Horarios y turnos', permission: 'schedule:manage' },
];

/**
 * Panel de indicadores (RF-24): tres tarjetas compuestas con la métrica exacta que expone el
 * backend, y accesos rápidos a las secciones que el usuario puede operar. Sin gráficas ni
 * comparativos: duplicarían las mismas cinco cifras ya visibles en las tarjetas.
 */
@Component({
  selector: 'app-metrics-dashboard',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatButtonModule, PageHeaderComponent, EmptyStateComponent],
  template: `
    <app-page-header title="Panel" subtitle="Resumen operativo de tu empresa" />

    @if (summary(); as s) {
      <div class="card-row">
        <div class="metric-card">
          <div class="metric-head">
            <span class="metric-icon info"><mat-icon>group</mat-icon></span>
            <h3>Asistencia de hoy</h3>
          </div>
          <div class="metric-split">
            <div class="metric-item">
              <div class="metric-value">{{ s.attendanceTodayAccepted }}</div>
              <span class="status-chip success">Aceptados</span>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ s.attendanceTodayRejected }}</div>
              <span class="status-chip danger">Rechazados</span>
            </div>
          </div>
        </div>

        <div class="metric-card highlight-danger">
          <div class="metric-head">
            <span class="metric-icon danger"><mat-icon>report_problem</mat-icon></span>
            <h3>Incidencias abiertas</h3>
          </div>
          <div class="metric-value big">{{ s.openIncidents }}</div>
          <p class="metric-sub">Requieren revisión del supervisor</p>
          @if (can('incident:approve')) {
            <a mat-flat-button routerLink="/incidents" class="full-width danger-button">
              <mat-icon>list</mat-icon> Ver incidencias
            </a>
          }
        </div>

        <div class="metric-card">
          <div class="metric-head">
            <span class="metric-icon info"><mat-icon>verified</mat-icon></span>
            <h3>Operación activa</h3>
          </div>
          <div class="metric-split">
            <div class="metric-item">
              <div class="metric-value">{{ s.activeUsers }}</div>
              <span class="metric-label">Usuarios activos</span>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ s.activeWorkSites }}</div>
              <span class="metric-label">Centros activos</span>
            </div>
          </div>
        </div>
      </div>

      @if (visibleLinks().length > 0) {
        <h3 class="section-title">Accesos rápidos</h3>
        <div class="quick-grid">
          @for (link of visibleLinks(); track link.route) {
            <a class="quick-link" [routerLink]="link.route">
              <span class="quick-icon"><mat-icon>{{ link.icon }}</mat-icon></span>
              <span class="quick-label">{{ link.label }}</span>
            </a>
          }
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
      .card-row {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: var(--sp-4);
      }
      .metric-card {
        background: var(--surface);
        border: 1px solid var(--border);
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-1);
        padding: var(--sp-5);
      }
      .metric-card.highlight-danger { border-color: var(--danger); background: var(--danger-bg); }

      .metric-head { display: flex; align-items: center; gap: var(--sp-3); margin-bottom: var(--sp-4); }
      .metric-head h3 { margin: 0; font-size: var(--font-card-title); font-weight: 700; }
      .metric-icon {
        width: 38px; height: 38px; border-radius: var(--radius-md);
        display: grid; place-items: center; flex: 0 0 auto;
      }
      .metric-icon.info { color: var(--info); background: var(--info-bg); }
      .metric-icon.danger { color: var(--danger); background: var(--danger-bg); }

      .metric-split { display: flex; gap: var(--sp-5); }
      .metric-item { display: flex; flex-direction: column; gap: 6px; }
      .metric-value { font-size: 1.8rem; font-weight: 700; line-height: 1; }
      .metric-value.big { font-size: 2.4rem; margin-bottom: var(--sp-1); }
      .metric-label { font-size: var(--font-small); color: var(--text-muted); }
      .metric-sub { font-size: var(--font-small); color: var(--text-muted); margin: 0 0 var(--sp-4); }
      .danger-button { background: var(--danger) !important; color: #fff !important; }

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
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
        gap: var(--sp-3);
      }
      .quick-link {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
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
      .quick-icon {
        width: 36px; height: 36px; border-radius: var(--radius-md);
        display: grid; place-items: center;
        background: var(--brand-soft); color: var(--brand);
      }
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

  protected can(permission: string): boolean {
    return this.store.hasPermission(permission);
  }
}
