import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import * as L from 'leaflet';
import { toDataURL } from 'qrcode';

import { NotificationService } from '../../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../../core/ui/page-header.component';
import { WorkSiteService } from '../work-site.service';
import { QrToken } from './geofence.models';
import { GeofenceService } from './geofence.service';

/**
 * Geocerca circular por centro (RF-10) con selector de mapa (clic para fijar el centro),
 * y generación/rotación del QR firmado del centro (RF-14). Requiere {@code geofence:manage}.
 */
@Component({
  selector: 'app-geofence',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header title="Geocerca y QR del centro" subtitle="Define el radio permitido y genera el QR de acceso">
      <a mat-stroked-button routerLink="/work-sites"><mat-icon>arrow_back</mat-icon> Volver</a>
    </app-page-header>

    @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
    @if (error()) { <p class="error-text">{{ error() }}</p> }

    <div style="display:flex;gap:16px;flex-wrap:wrap">
      <mat-card style="flex:1 1 480px">
        <mat-card-header><mat-card-title>Geocerca circular</mat-card-title></mat-card-header>
        <mat-card-content>
          <p class="muted">Hacé clic en el mapa para fijar el centro; ajustá el radio en metros.</p>
          <div #mapEl style="height:340px;border-radius:8px;overflow:hidden"></div>
          <form [formGroup]="form" (ngSubmit)="save()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:baseline;margin-top:12px">
            <mat-form-field appearance="outline" style="width:150px">
              <mat-label>Latitud</mat-label>
              <input matInput type="number" step="any" formControlName="latitude" (change)="syncMap()" />
            </mat-form-field>
            <mat-form-field appearance="outline" style="width:150px">
              <mat-label>Longitud</mat-label>
              <input matInput type="number" step="any" formControlName="longitude" (change)="syncMap()" />
            </mat-form-field>
            <mat-form-field appearance="outline" style="width:150px">
              <mat-label>Radio (m)</mat-label>
              <input matInput type="number" step="any" formControlName="radiusM" (change)="syncMap()" />
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || loading()">
              Guardar geocerca
            </button>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card style="flex:1 1 280px">
        <mat-card-header><mat-card-title>QR del centro</mat-card-title></mat-card-header>
        <mat-card-content>
          <p class="muted">El QR es un token firmado con vigencia; generarlo de nuevo rota el anterior.</p>
          <form [formGroup]="qrForm" (ngSubmit)="generateQr()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:baseline">
            <mat-form-field appearance="outline" style="width:150px">
              <mat-label>Vigencia (min)</mat-label>
              <input matInput type="number" min="1" max="1440" step="1" formControlName="ttlMinutes" />
              <mat-hint>1 a 1440 minutos</mat-hint>
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" [disabled]="loading() || qrForm.invalid">
              <mat-icon>qr_code_2</mat-icon> Generar / rotar QR
            </button>
          </form>
          @if (qr(); as q) {
            <div style="margin-top:16px;text-align:center">
              @if (qrImage(); as img) {
                <img [src]="img" alt="QR del centro" style="width:220px;height:220px" />
              }
              <div class="muted" style="margin-top:8px">Vence: {{ q.expiresAt }}</div>
              <textarea readonly style="width:100%;margin-top:8px;font-family:monospace;font-size:.75rem" rows="3">{{ q.token }}</textarea>
            </div>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
})
export class GeofenceComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapEl') private mapEl!: ElementRef<HTMLDivElement>;

  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly geofence = inject(GeofenceService);
  private readonly workSites = inject(WorkSiteService);
  private readonly notify = inject(NotificationService);

  private readonly workSiteId = this.route.snapshot.paramMap.get('id') ?? '';
  private map?: L.Map;
  private centerMarker?: L.CircleMarker;
  private radiusCircle?: L.Circle;

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly qr = signal<QrToken | null>(null);
  protected readonly qrImage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    latitude: this.fb.nonNullable.control(0, [Validators.required]),
    longitude: this.fb.nonNullable.control(0, [Validators.required]),
    radiusM: this.fb.nonNullable.control(100, [Validators.required, Validators.min(1)]),
  });

  protected readonly qrForm = this.fb.nonNullable.group({
    ttlMinutes: this.fb.nonNullable.control(2, [Validators.required, Validators.min(1), Validators.max(1440)]),
  });

  ngAfterViewInit(): void {
    this.map = L.map(this.mapEl.nativeElement).setView([0, 0], 2);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap',
      maxZoom: 19,
    }).addTo(this.map);
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.form.patchValue({ latitude: e.latlng.lat, longitude: e.latlng.lng });
      this.syncMap();
    });
    this.load();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private load(): void {
    if (!this.workSiteId) {
      return;
    }
    this.loading.set(true);
    // Centro por defecto: ubicación del propio centro de trabajo.
    this.workSites.get(this.workSiteId).subscribe({
      next: (site) => {
        this.form.patchValue({ latitude: site.latitude, longitude: site.longitude });
        this.syncMap(15);
        // Geocerca existente (si la hay) tiene prioridad.
        this.geofence.get(this.workSiteId).subscribe({
          next: (g) => {
            this.form.patchValue({ latitude: g.latitude, longitude: g.longitude, radiusM: g.radiusM });
            this.syncMap(16);
            this.loading.set(false);
          },
          error: () => this.loading.set(false), // sin geocerca previa
        });
      },
      error: () => {
        this.error.set('No se pudo cargar el centro de trabajo.');
        this.loading.set(false);
      },
    });
  }

  /** Refleja los valores del formulario en el mapa (marcador de centro + círculo de radio). */
  protected syncMap(zoom?: number): void {
    if (!this.map) {
      return;
    }
    const { latitude, longitude, radiusM } = this.form.getRawValue();
    const center: L.LatLngExpression = [latitude, longitude];
    if (!this.centerMarker) {
      this.centerMarker = L.circleMarker(center, { radius: 6, color: '#3949ab' }).addTo(this.map);
      this.radiusCircle = L.circle(center, { radius: radiusM, color: '#3949ab', fillOpacity: 0.1 }).addTo(this.map);
    } else {
      this.centerMarker.setLatLng(center);
      this.radiusCircle?.setLatLng(center);
      this.radiusCircle?.setRadius(radiusM);
    }
    this.map.setView(center, zoom ?? this.map.getZoom());
  }

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.geofence.upsert(this.workSiteId, this.form.getRawValue()).subscribe({
      next: () => {
        this.notify.success('Geocerca guardada.');
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.notify.error('No se pudo guardar la geocerca.');
      },
    });
  }

  protected generateQr(): void {
    if (this.qrForm.invalid) {
      return;
    }
    this.loading.set(true);
    this.geofence.generateQr(this.workSiteId, this.qrForm.getRawValue()).subscribe({
      next: (token) => {
        this.qr.set(token);
        void toDataURL(token.token, { width: 220 }).then((url) => this.qrImage.set(url));
        this.notify.success('QR generado.');
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.notify.error('No se pudo generar el QR.');
      },
    });
  }
}
