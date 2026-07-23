import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';

export interface MapPickerData {
  latitude: number | null;
  longitude: number | null;
}

export interface MapPickerResult {
  latitude: number;
  longitude: number;
}

/**
 * Diálogo para elegir una ubicación (lat/long) haciendo clic en un mapa Leaflet (OSM).
 * Devuelve {@link MapPickerResult} al confirmar, o {@code undefined} al cancelar.
 */
@Component({
  selector: 'app-map-picker-dialog',
  standalone: true,
  imports: [DecimalPipe, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Seleccionar ubicación</h2>
    <mat-dialog-content>
      <p class="muted" style="margin-top:0">Hacé clic en el mapa para fijar la ubicación del centro.</p>
      <div #mapEl style="height:360px;border-radius:8px;overflow:hidden"></div>
      @if (selected(); as s) {
        <p style="margin-bottom:0">
          Ubicación elegida: {{ s.latitude | number: '1.4-6' }}, {{ s.longitude | number: '1.4-6' }}
        </p>
      } @else {
        <p class="muted" style="margin-bottom:0">Aún no seleccionaste ninguna ubicación.</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="undefined">Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="!selected()" [mat-dialog-close]="selected()">
        <mat-icon>place</mat-icon> Usar ubicación
      </button>
    </mat-dialog-actions>
  `,
})
export class MapPickerDialogComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapEl') private mapEl!: ElementRef<HTMLDivElement>;

  private readonly data = inject<MapPickerData>(MAT_DIALOG_DATA);
  private map?: L.Map;
  private marker?: L.Marker;

  protected readonly selected = signal<MapPickerResult | null>(null);

  ngAfterViewInit(): void {
    const hasInitial = this.data.latitude != null && this.data.longitude != null;
    this.map = L.map(this.mapEl.nativeElement);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap',
      maxZoom: 19,
    }).addTo(this.map);

    if (hasInitial) {
      const lat = this.data.latitude as number;
      const lng = this.data.longitude as number;
      this.map.setView([lat, lng], 15);
      this.setPoint(lat, lng);
    } else {
      this.map.setView([0, 0], 2);
    }

    this.map.on('click', (e: L.LeafletMouseEvent) => this.setPoint(e.latlng.lat, e.latlng.lng));

    // El diálogo arranca animándose: recalcular el tamaño para que las tiles no queden grises.
    setTimeout(() => this.map?.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private setPoint(latitude: number, longitude: number): void {
    const point: L.LatLngExpression = [latitude, longitude];
    if (!this.marker) {
      this.marker = L.marker(point).addTo(this.map as L.Map);
    } else {
      this.marker.setLatLng(point);
    }
    this.selected.set({ latitude, longitude });
  }
}
