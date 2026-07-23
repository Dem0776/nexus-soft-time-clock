import { AfterViewInit, Component, ElementRef, ViewChild, computed, inject, signal, OnDestroy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import * as L from 'leaflet';

import { EmptyStateComponent } from '../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../core/ui/page-header.component';
import { WorkSite } from '../admin/work-sites/work-site.models';
import { WorkSiteService } from '../admin/work-sites/work-site.service';
import { AttendanceEvent, RealtimeService } from './realtime.service';

/**
 * Mapa en tiempo real (RF-25, CU-11): grafica los registros de asistencia aceptados sobre
 * los centros del tenant a medida que llegan por WebSocket, y mantiene un feed en vivo.
 * Los eventos rechazados no incluyen ubicación, por lo que solo aparecen en el feed.
 */
@Component({
  selector: 'app-realtime-map',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatListModule, PageHeaderComponent, EmptyStateComponent],
  template: `
    <app-page-header title="Mapa en tiempo real" subtitle="Visualiza los fichajes de tu operación a medida que ocurren">
      <span class="conn-badge" [class.on]="connected()">
        <mat-icon>{{ connected() ? 'sensors' : 'sensors_off' }}</mat-icon>
        {{ connected() ? 'Conectado en tiempo real' : 'Reconectando…' }}
      </span>
    </app-page-header>

    <div class="map-layout">
      <mat-card class="map-card">
        <mat-card-content>
          <div #mapEl class="map-canvas"></div>
        </mat-card-content>
      </mat-card>

      <mat-card class="feed-card">
        <mat-card-header>
          <mat-card-title>Eventos recientes</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="feed-stats">
            <span class="stat success"><mat-icon>check_circle</mat-icon> {{ acceptedCount() }} aceptados</span>
            <span class="stat danger"><mat-icon>cancel</mat-icon> {{ rejectedCount() }} rechazados</span>
          </div>
          @if (feed().length === 0) {
            <app-empty-state icon="sensors" message="Esperando eventos de asistencia…" />
          }
          <mat-list>
            @for (e of feed(); track e.attendanceId) {
              <mat-list-item>
                <mat-icon matListItemIcon [style.color]="e.type === 'ACCEPTED' ? 'var(--success)' : 'var(--danger)'">
                  {{ e.type === 'ACCEPTED' ? 'check_circle' : 'cancel' }}
                </mat-icon>
                <span matListItemTitle>{{ e.type === 'ACCEPTED' ? (e.eventKind || 'Registro') : (e.reason || 'Rechazo') }}</span>
                <span matListItemLine class="muted">{{ e.occurredAt }}</span>
              </mat-list-item>
            }
          </mat-list>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .conn-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: var(--font-small);
        font-weight: 600;
        padding: 4px 12px;
        border-radius: 999px;
        color: var(--neutral);
        background: var(--neutral-bg);
      }
      .conn-badge.on { color: var(--success); background: var(--success-bg); }
      .conn-badge mat-icon { font-size: 16px; width: 16px; height: 16px; }

      .map-layout { display: flex; gap: var(--sp-4); flex-wrap: wrap; }
      .map-card { flex: 2 1 520px; }
      .feed-card { flex: 1 1 300px; }
      .map-canvas { height: 460px; border-radius: var(--radius-md); overflow: hidden; }

      .feed-stats { display: flex; gap: var(--sp-3); margin-bottom: var(--sp-3); }
      .stat {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: var(--font-small);
        font-weight: 600;
        padding: 3px 10px;
        border-radius: 999px;
      }
      .stat.success { color: var(--success); background: var(--success-bg); }
      .stat.danger { color: var(--danger); background: var(--danger-bg); }
      .stat mat-icon { font-size: 14px; width: 14px; height: 14px; }
    `,
  ],
})
export class RealtimeMapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapEl') private mapEl!: ElementRef<HTMLDivElement>;

  private readonly workSiteService = inject(WorkSiteService);
  private readonly realtime = inject(RealtimeService);

  protected readonly connected = signal(false);
  protected readonly feed = signal<AttendanceEvent[]>([]);
  protected readonly acceptedCount = computed(() => this.feed().filter((e) => e.type === 'ACCEPTED').length);
  protected readonly rejectedCount = computed(() => this.feed().filter((e) => e.type === 'REJECTED').length);

  private map?: L.Map;
  private readonly siteMarkers = new Map<string, L.CircleMarker>();

  ngAfterViewInit(): void {
    this.map = L.map(this.mapEl.nativeElement).setView([0, 0], 2);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap',
      maxZoom: 19,
    }).addTo(this.map);

    this.workSiteService.list(0, 500).subscribe({
      next: (result) => this.plotSites(result.content),
      error: () => void 0,
    });

    this.realtime.connect(
      (event) => this.onEvent(event),
      (connected) => this.connected.set(connected),
    );
  }

  ngOnDestroy(): void {
    this.realtime.disconnect();
    this.map?.remove();
  }

  private plotSites(sites: WorkSite[]): void {
    if (!this.map) {
      return;
    }
    const bounds: L.LatLngExpression[] = [];
    for (const site of sites) {
      const marker = L.circleMarker([site.latitude, site.longitude], {
        radius: 8,
        color: '#3949ab',
        fillColor: '#3949ab',
        fillOpacity: 0.4,
      })
        .addTo(this.map)
        .bindTooltip(site.name);
      this.siteMarkers.set(site.id, marker);
      bounds.push([site.latitude, site.longitude]);
    }
    if (bounds.length > 0) {
      this.map.fitBounds(L.latLngBounds(bounds).pad(0.2));
    }
  }

  private onEvent(event: AttendanceEvent): void {
    this.feed.update((list) => [event, ...list].slice(0, 30));
    if (event.type !== 'ACCEPTED' || !event.workSiteId) {
      return;
    }
    const marker = this.siteMarkers.get(event.workSiteId);
    if (marker) {
      this.pulse(marker);
    }
  }

  /** Resalta brevemente el centro que recibió un registro. */
  private pulse(marker: L.CircleMarker): void {
    marker.setStyle({ color: '#0f7a52', fillColor: '#0f7a52', radius: 14 });
    setTimeout(() => marker.setStyle({ color: '#3949ab', fillColor: '#3949ab', radius: 8 }), 1200);
  }
}
