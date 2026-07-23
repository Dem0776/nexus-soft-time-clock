import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ConfirmDialogComponent } from '../../../core/ui/confirm-dialog.component';
import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { MapPickerDialogComponent, MapPickerResult } from '../../../core/ui/map-picker-dialog.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { WorkSite } from './work-site.models';
import { WorkSiteService } from './work-site.service';

/** Administración de centros de trabajo (RF-07): CRUD, estado y acceso a geocerca/QR. */
@Component({
  selector: 'app-work-sites',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatProgressBarModule,
    MatTableModule,
    MatPaginatorModule,
    MatDialogModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Centros de trabajo" subtitle="Administra los centros de trabajo de la empresa">
      <button mat-flat-button color="primary" (click)="startCreate()">
        <mat-icon>add_location_alt</mat-icon> Nuevo centro
      </button>
    </app-page-header>

    <div class="split-layout">
      <div class="split-main">
        <mat-card>
          <mat-card-content>
            @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
            @if (error()) { <p class="error-text">{{ error() }}</p> }

            <div class="table-wrap">
              <table mat-table [dataSource]="sites()" style="width:100%">
                <ng-container matColumnDef="code">
                  <th mat-header-cell *matHeaderCellDef>Código</th>
                  <td mat-cell *matCellDef="let s">{{ s.code }}</td>
                </ng-container>
                <ng-container matColumnDef="name">
                  <th mat-header-cell *matHeaderCellDef>Nombre</th>
                  <td mat-cell *matCellDef="let s">{{ s.name }}</td>
                </ng-container>
                <ng-container matColumnDef="location">
                  <th mat-header-cell *matHeaderCellDef>Ubicación</th>
                  <td mat-cell *matCellDef="let s">{{ s.latitude | number: '1.4-6' }}, {{ s.longitude | number: '1.4-6' }}</td>
                </ng-container>
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Estado</th>
                  <td mat-cell *matCellDef="let s"><app-status-chip [status]="s.status" /></td>
                </ng-container>
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef></th>
                  <td mat-cell *matCellDef="let s" style="text-align:right;white-space:nowrap">
                    <a mat-icon-button [routerLink]="['/work-sites', s.id, 'geofence']" matTooltip="Geocerca y QR" (click)="$event.stopPropagation()">
                      <mat-icon>my_location</mat-icon>
                    </a>
                    @if (s.status === 'ACTIVE') {
                      <button mat-icon-button (click)="toggleStatus(s); $event.stopPropagation()" matTooltip="Desactivar"><mat-icon>block</mat-icon></button>
                    } @else {
                      <button mat-icon-button (click)="toggleStatus(s); $event.stopPropagation()" matTooltip="Activar"><mat-icon>check_circle</mat-icon></button>
                    }
                  </td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="columns"></tr>
                <tr mat-row *matRowDef="let row; columns: columns"
                    (click)="startEdit(row)"
                    [style.background]="row.id === editingId() ? 'var(--brand-soft)' : ''"
                    style="cursor:pointer"></tr>
              </table>
            </div>

            @if (!loading() && sites().length === 0) {
              <app-empty-state icon="place" message="No hay centros de trabajo para mostrar." />
            }

            <mat-paginator
              [length]="total()"
              [pageSize]="size()"
              [pageIndex]="page()"
              [pageSizeOptions]="[10, 20, 50]"
              (page)="onPage($event)"
            />
          </mat-card-content>
        </mat-card>
      </div>

      @if (showForm()) {
        <aside class="split-drawer">
          <div class="drawer-header">
            <div class="titles"><h3>{{ editingId() ? 'Editar centro' : 'Nuevo centro' }}</h3></div>
            <button mat-icon-button (click)="cancelForm()" aria-label="Cerrar"><mat-icon>close</mat-icon></button>
          </div>
          <div class="drawer-body">
            <form [formGroup]="form" style="display:flex;flex-direction:column">
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Código</mat-label>
                <input matInput formControlName="code" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Nombre</mat-label>
                <input matInput formControlName="name" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Dirección</mat-label>
                <input matInput formControlName="address" />
              </mat-form-field>
              <div class="drawer-row">
                <mat-form-field appearance="outline" class="drawer-field">
                  <mat-label>Latitud</mat-label>
                  <input matInput type="number" step="any" formControlName="latitude" readonly />
                </mat-form-field>
                <mat-form-field appearance="outline" class="drawer-field">
                  <mat-label>Longitud</mat-label>
                  <input matInput type="number" step="any" formControlName="longitude" readonly />
                </mat-form-field>
              </div>
              <button mat-stroked-button type="button" (click)="pickLocation()" class="full-width" style="margin-bottom:8px">
                <mat-icon>place</mat-icon> Seleccionar en el mapa
              </button>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Zona horaria</mat-label>
                <input matInput formControlName="timezone" placeholder="America/Lima" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="drawer-field">
                <mat-label>Precisión GPS máx (m)</mat-label>
                <input matInput type="number" formControlName="gpsAccuracyMaxM" />
              </mat-form-field>
              <mat-checkbox formControlName="requirePhoto">Foto obligatoria</mat-checkbox>
              <mat-checkbox formControlName="requireBiometric" style="margin-top:8px">Biometría obligatoria</mat-checkbox>
            </form>
          </div>
          <div class="drawer-actions">
            <button mat-button (click)="cancelForm()">Cancelar</button>
            <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="save()">
              {{ editingId() ? 'Guardar' : 'Crear' }}
            </button>
          </div>
        </aside>
      }
    </div>
  `,
  styles: [`.drawer-row { display: flex; gap: var(--sp-3); } .drawer-row mat-form-field { flex: 1; }`],
})
export class WorkSitesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(WorkSiteService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['code', 'name', 'location', 'status', 'actions'];
  protected readonly sites = signal<WorkSite[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly showForm = signal(false);
  protected readonly editingId = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly form = this.fb.group({
    code: this.fb.nonNullable.control('', [Validators.required]),
    name: this.fb.nonNullable.control('', [Validators.required]),
    address: this.fb.nonNullable.control(''),
    latitude: this.fb.control<number | null>(null, [Validators.required]),
    longitude: this.fb.control<number | null>(null, [Validators.required]),
    timezone: this.fb.nonNullable.control(''),
    gpsAccuracyMaxM: this.fb.control<number | null>(null),
    requirePhoto: this.fb.nonNullable.control(false),
    requireBiometric: this.fb.nonNullable.control(false),
  });

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(this.page(), this.size()).subscribe({
      next: (result) => {
        this.sites.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el listado (¿permiso worksite:manage?).');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset({ requirePhoto: false, requireBiometric: false });
    this.form.controls.code.enable();
    this.showForm.set(true);
  }

  protected startEdit(site: WorkSite): void {
    this.editingId.set(site.id);
    this.form.reset({
      code: site.code,
      name: site.name,
      address: site.address ?? '',
      latitude: site.latitude,
      longitude: site.longitude,
      timezone: site.timezone ?? '',
      gpsAccuracyMaxM: site.gpsAccuracyMaxM ?? null,
      requirePhoto: site.requirePhoto ?? false,
      requireBiometric: site.requireBiometric ?? false,
    });
    this.form.controls.code.disable(); // el código es inmutable
    this.showForm.set(true);
  }

  protected pickLocation(): void {
    const { latitude, longitude } = this.form.getRawValue();
    this.dialog
      .open(MapPickerDialogComponent, {
        width: '640px',
        data: { latitude, longitude },
      })
      .afterClosed()
      .subscribe((result: MapPickerResult | undefined) => {
        if (result) {
          this.form.patchValue({ latitude: result.latitude, longitude: result.longitude });
          this.form.markAsDirty();
        }
      });
  }

  protected cancelForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.form.controls.code.enable();
    this.form.reset({ requirePhoto: false, requireBiometric: false });
  }

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    const common = {
      name: raw.name,
      address: raw.address || undefined,
      latitude: raw.latitude as number,
      longitude: raw.longitude as number,
      timezone: raw.timezone || undefined,
      gpsAccuracyMaxM: raw.gpsAccuracyMaxM ?? undefined,
      requirePhoto: raw.requirePhoto,
      requireBiometric: raw.requireBiometric,
    };
    this.loading.set(true);
    const editId = this.editingId();
    const request$ = editId
      ? this.service.update(editId, common)
      : this.service.create({ code: raw.code, ...common });
    request$.subscribe({
      next: () => {
        this.notify.success(editId ? 'Centro actualizado.' : 'Centro creado.');
        this.cancelForm();
        this.reload();
      },
      error: () => {
        this.loading.set(false);
        this.notify.error('No se pudo guardar el centro.');
      },
    });
  }

  protected toggleStatus(site: WorkSite): void {
    const next = site.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: next === 'INACTIVE' ? 'Desactivar centro' : 'Activar centro',
          message: `¿Confirmás cambiar el estado de "${site.name}" a ${next}?`,
          color: next === 'INACTIVE' ? 'warn' : 'primary',
        },
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.service.setStatus(site.id, next).subscribe({
          next: () => {
            this.notify.success('Estado actualizado.');
            this.reload();
          },
          error: () => this.notify.error('No se pudo cambiar el estado.'),
        });
      });
  }
}
