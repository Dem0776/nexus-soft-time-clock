import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import * as L from 'leaflet';

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
  imports: [MatCardModule, MatIconModule, MatListModule, PageHeaderComponent],
  template: `
    <app-page-header title="Mapa en tiempo real">
      <span [style.color]="connected() ? '#2e7d32' : '#b3261e'">
        <mat-icon style="vertical-align:middle">{{ connected() ? 'sensors' : 'sensors_off' }}</mat-icon>
        {{ connected() ? 'Conectado' : 'Desconectado' }}
      </span>
    </app-page-header>

    <div style="display:flex;gap:16px;flex-wrap:wrap">
      <mat-card style="flex:2 1 520px">
        <mat-card-content>
          <div #mapEl style="height:460px;border-radius:8px;overflow:hidden"></div>
        </mat-card-content>
      </mat-card>

      <mat-card style="flex:1 1 300px">
        <mat-card-header><mat-card-title>Eventos recientes</mat-card-title></mat-card-header>
        <mat-card-content>
          @if (feed().length === 0) {
            <p class="muted">Esperando eventos de asistencia…</p>
          }
          <mat-list>
            @for (e of feed(); track e.attendanceId) {
              <mat-list-item>
                <mat-icon matListItemIcon [style.color]="e.type === 'ACCEPTED' ? '#2e7d32' : '#b3261e'">
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
})
export class RealtimeMapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapEl') private mapEl!: ElementRef<HTMLDivElement>;

  private readonly workSiteService = inject(WorkSiteService);
  private readonly realtime = inject(RealtimeService);

  protected readonly connected = signal(false);
  protected readonly feed = signal<AttendanceEvent[]>([]);

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
        color: '#1565c0',
        fillColor: '#1565c0',
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
    marker.setStyle({ color: '#2e7d32', fillColor: '#2e7d32', radius: 14 });
    setTimeout(() => marker.setStyle({ color: '#1565c0', fillColor: '#1565c0', radius: 8 }), 1200);
  }
}
