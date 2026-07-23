import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../core/ui/empty-state.component';
import { NotificationService } from '../../core/ui/notification.service';
import { PageHeaderComponent } from '../../core/ui/page-header.component';
import { StatusChipComponent } from '../../core/ui/status-chip.component';
import { INCIDENT_RESOLUTIONS, Incident, ResolveIncident } from './incident.models';
import { IncidentService } from './incident.service';

/**
 * Bandeja de incidencias (RF-09, CU-08): tabla + drawer de resolución. El drawer permite
 * encadenar revisiones ("Guardar y abrir siguiente") para el flujo de alta frecuencia del
 * supervisor sin reabrir un diálogo por cada fichaje rechazado.
 */
@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressBarModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Incidencias" subtitle="Revisa y resuelve fichajes rechazados" />

    <div class="split-layout">
      <div class="split-main">
        <mat-card>
          <mat-card-content>
            <div class="filter-bar">
              <mat-form-field appearance="outline" class="search">
                <mat-icon matPrefix>search</mat-icon>
                <mat-label>Buscar por tipo, motivo o ID</mat-label>
                <input matInput [formControl]="searchControl" autocomplete="off" />
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:200px">
                <mat-label>Prioridad</mat-label>
                <mat-select [formControl]="priorityFilter">
                  <mat-option value="">Todas</mat-option>
                  @for (p of priorities(); track p) { <mat-option [value]="p">{{ p }}</mat-option> }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:200px">
                <mat-label>Estado</mat-label>
                <mat-select [formControl]="statusFilter" (selectionChange)="applyServerFilter()">
                  <mat-option value="">Todas</mat-option>
                  <mat-option value="OPEN">Abiertas</mat-option>
                  <mat-option value="APPROVED">Aprobadas</mat-option>
                  <mat-option value="REJECTED">Rechazadas</mat-option>
                  <mat-option value="RESOLVED">Resueltas</mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
            @if (error()) { <p class="error-text">{{ error() }}</p> }

            <div class="table-wrap">
              <table mat-table [dataSource]="filtered()" style="width:100%">
                <ng-container matColumnDef="priority">
                  <th mat-header-cell *matHeaderCellDef>Prioridad</th>
                  <td mat-cell *matCellDef="let i">{{ i.priority }}</td>
                </ng-container>
                <ng-container matColumnDef="date">
                  <th mat-header-cell *matHeaderCellDef>Fecha</th>
                  <td mat-cell *matCellDef="let i">{{ i.incidentDate }}</td>
                </ng-container>
                <ng-container matColumnDef="type">
                  <th mat-header-cell *matHeaderCellDef>Tipo</th>
                  <td mat-cell *matCellDef="let i">{{ i.type }}</td>
                </ng-container>
                <ng-container matColumnDef="description">
                  <th mat-header-cell *matHeaderCellDef>Motivo</th>
                  <td mat-cell *matCellDef="let i">{{ i.description || '—' }}</td>
                </ng-container>
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Estado</th>
                  <td mat-cell *matCellDef="let i"><app-status-chip [status]="i.status" /></td>
                </ng-container>
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef></th>
                  <td mat-cell *matCellDef="let i" style="text-align:right">
                    <button mat-stroked-button (click)="review(i)">Revisar</button>
                  </td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="columns"></tr>
                <tr mat-row *matRowDef="let row; columns: columns"
                    [style.background]="row.id === selected()?.id ? 'var(--brand-soft)' : ''"></tr>
              </table>
            </div>

            @if (!loading() && filtered().length === 0) {
              <app-empty-state icon="inbox" message="No hay incidencias para mostrar." />
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

      @if (selected(); as i) {
        <aside class="split-drawer">
          <div class="drawer-header">
            <div class="titles">
              <h3>Incidencia · {{ i.type }}</h3>
              <p class="sub">{{ i.incidentDate }} · Prioridad {{ i.priority }}</p>
            </div>
            <button mat-icon-button (click)="closeDrawer()" aria-label="Cerrar"><mat-icon>close</mat-icon></button>
          </div>
          <div class="drawer-body">
            <div class="detail-section-title">Información</div>
            <div class="detail-grid">
              <div class="detail-row"><span class="k">Usuario</span><span class="v">{{ i.userId }}</span></div>
              <div class="detail-row"><span class="k">Estado actual</span><span class="v"><app-status-chip [status]="i.status" /></span></div>
              @if (i.relatedAttendanceId) {
                <div class="detail-row"><span class="k">Fichaje relacionado</span><span class="v">{{ i.relatedAttendanceId }}</span></div>
              }
              @if (i.description) {
                <div class="detail-row"><span class="k">Motivo</span><span class="v">{{ i.description }}</span></div>
              }
              @if (i.resolvedBy) {
                <div class="detail-row"><span class="k">Resuelto por</span><span class="v">{{ i.resolvedBy }}</span></div>
                <div class="detail-row"><span class="k">Resuelto el</span><span class="v">{{ i.resolvedAt }}</span></div>
              }
              @if (i.resolutionNote) {
                <div class="detail-row"><span class="k">Nota</span><span class="v">{{ i.resolutionNote }}</span></div>
              }
            </div>

            @if (i.status === 'OPEN') {
              <div class="detail-section-title">Resolución</div>
              <form [formGroup]="resolveForm">
                <mat-form-field appearance="outline" class="drawer-field">
                  <mat-label>Resultado</mat-label>
                  <mat-select formControlName="status">
                    @for (r of resolutions; track r) { <mat-option [value]="r">{{ r }}</mat-option> }
                  </mat-select>
                </mat-form-field>
                <mat-form-field appearance="outline" class="drawer-field">
                  <mat-label>Nota de resolución</mat-label>
                  <textarea matInput formControlName="note" rows="3"></textarea>
                </mat-form-field>
              </form>
            }
          </div>
          @if (i.status === 'OPEN') {
            <div class="drawer-actions">
              <button mat-button (click)="closeDrawer()">Cancelar</button>
              <button mat-flat-button color="primary" [disabled]="resolveForm.invalid || saving()" (click)="save(i, false)">
                Guardar
              </button>
              <button mat-flat-button color="primary" [disabled]="resolveForm.invalid || saving()" (click)="save(i, true)">
                Guardar y abrir siguiente <mat-icon iconPositionEnd>arrow_forward</mat-icon>
              </button>
            </div>
          }
        </aside>
      }
    </div>
  `,
})
export class IncidentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(IncidentService);
  private readonly notify = inject(NotificationService);

  protected readonly columns = ['priority', 'date', 'type', 'description', 'status', 'actions'];
  protected readonly resolutions = INCIDENT_RESOLUTIONS;
  protected readonly incidents = signal<Incident[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly selected = signal<Incident | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly statusFilter = new FormControl('', { nonNullable: true });
  protected readonly priorityFilter = new FormControl('', { nonNullable: true });
  protected readonly searchControl = new FormControl('', { nonNullable: true });

  protected readonly priorities = computed(() => [...new Set(this.incidents().map((i) => i.priority))].sort());

  protected readonly filtered = computed(() => {
    const priority = this.priorityFilter.value;
    const search = this.searchControl.value.trim().toLowerCase();
    return this.incidents().filter((i) => {
      if (priority && i.priority !== priority) return false;
      if (!search) return true;
      const haystack = `${i.type} ${i.description ?? ''} ${i.id}`.toLowerCase();
      return haystack.includes(search);
    });
  });

  protected readonly resolveForm = this.fb.nonNullable.group({
    status: ['APPROVED', [Validators.required]],
    note: ['', [Validators.required]],
  });

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(this.statusFilter.value || undefined, this.page(), this.size()).subscribe({
      next: (result) => {
        this.incidents.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la bandeja (¿permiso incident:approve?).');
        this.loading.set(false);
      },
    });
  }

  protected applyServerFilter(): void {
    this.page.set(0);
    this.closeDrawer();
    this.reload();
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected review(incident: Incident): void {
    this.selected.set(incident);
    this.resolveForm.reset({ status: 'APPROVED', note: '' });
  }

  protected closeDrawer(): void {
    this.selected.set(null);
  }

  protected save(incident: Incident, openNext: boolean): void {
    if (this.resolveForm.invalid) {
      return;
    }
    const raw = this.resolveForm.getRawValue();
    const request: ResolveIncident = { status: raw.status as ResolveIncident['status'], note: raw.note };
    this.saving.set(true);
    this.service.resolve(incident.id, request).subscribe({
      next: () => {
        this.saving.set(false);
        this.notify.success('Incidencia resuelta.');
        const next = openNext ? this.nextOpenAfter(incident) : null;
        this.reload();
        if (next) {
          this.review(next);
        } else {
          this.closeDrawer();
        }
      },
      error: () => {
        this.saving.set(false);
        this.notify.error('No se pudo resolver la incidencia.');
      },
    });
  }

  private nextOpenAfter(current: Incident): Incident | null {
    const open = this.filtered().filter((i) => i.status === 'OPEN' && i.id !== current.id);
    return open[0] ?? null;
  }
}
