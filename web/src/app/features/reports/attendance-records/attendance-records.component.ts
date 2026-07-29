import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { AttendanceEventType } from '../../admin/event-types/event-type.models';
import { EventTypesService } from '../../admin/event-types/event-types.service';
import {
  AttendanceRecordReport,
  AttendanceRecordStatus,
  DEFAULT_EVENT_TYPE_LABELS,
  RecordSortColumn,
  RecordSortState,
  REJECTION_LABELS,
  STATUS_LABELS,
  STATUS_TONES,
} from './attendance-records.models';
import { AttendanceRecordsService } from './attendance-records.service';

interface ColumnDef {
  key: RecordSortColumn;
  label: string;
}

const COLUMNS: readonly ColumnDef[] = [
  { key: 'employeeNumber', label: 'N.º empleado' },
  { key: 'employeeName', label: 'Nombre' },
  { key: 'serverTime', label: 'Fecha y hora' },
  { key: 'eventType', label: 'Tipo de evento' },
  { key: 'status', label: 'Estado' },
  { key: 'workCenter', label: 'Centro de trabajo' },
];

const STATUS_OPTIONS: readonly AttendanceRecordStatus[] = ['ACCEPTED', 'REJECTED', 'PENDING_REVIEW'];

const isoDay = (d: Date): string => d.toISOString().slice(0, 10);

/**
 * Reporte de registros individuales de asistencia por empleado (RF-11). Muestra una fila por
 * marcación (número + nombre de empleado, fecha y hora, tipo de evento, estado y centro de trabajo),
 * sin exponer ids. Carga el rango de fechas del backend y aplica búsqueda, filtros (estado y tipo de
 * evento), ordenamiento y paginación en el cliente.
 */
@Component({
  selector: 'app-attendance-records',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatPaginatorModule,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header
      title="Registros de asistencia"
      subtitle="Marcaciones individuales por empleado (entrada, salida y eventos intermedios)"
    />

    <mat-card>
      <mat-card-content>
        <div class="toolbar" [formGroup]="form">
          <mat-form-field appearance="outline" class="search">
            <mat-icon matPrefix>search</mat-icon>
            <mat-label>Buscar (nombre, número o centro)</mat-label>
            <input matInput formControlName="search" autocomplete="off" />
          </mat-form-field>

          <div class="dates">
            <mat-form-field appearance="outline" class="date">
              <mat-label>Desde</mat-label>
              <input matInput type="date" formControlName="from" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="date">
              <mat-label>Hasta</mat-label>
              <input matInput type="date" formControlName="to" />
            </mat-form-field>
          </div>

          <mat-form-field appearance="outline" class="filter">
            <mat-label>Estado</mat-label>
            <mat-select formControlName="status">
              <mat-option value="ALL">Todos</mat-option>
              @for (s of statusOptions; track s) {
                <mat-option [value]="s">{{ statusLabels[s] }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="filter">
            <mat-label>Tipo de evento</mat-label>
            <mat-select formControlName="eventType">
              <mat-option value="ALL">Todos</mat-option>
              @for (t of eventTypeOptions; track t) {
                <mat-option [value]="t">{{ eventLabel(t) }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        </div>

        <div class="indicators">
          <span class="count">
            <strong>{{ filtered().length }}</strong>
            @if (filtered().length !== total()) { <span class="muted">de {{ total() }}</span> }
            registro{{ filtered().length === 1 ? '' : 's' }}
          </span>
        </div>

        @if (loading()) { <mat-progress-bar mode="indeterminate" class="loader-bar" /> }

        @if (loading()) {
          <div class="skeleton">
            @for (r of skeletonRows; track r) { <div class="skeleton-row"></div> }
          </div>
        } @else if (error()) {
          <div class="error-block">
            <mat-icon>cloud_off</mat-icon>
            <p>{{ error() }}</p>
            <button mat-flat-button color="primary" (click)="reload()">
              <mat-icon>refresh</mat-icon> Reintentar
            </button>
          </div>
        } @else if (filtered().length === 0) {
          <app-empty-state
            icon="search_off"
            [message]="total() === 0 ? 'No hay registros para el periodo seleccionado.' : 'Ningún registro coincide con los filtros aplicados.'"
          />
        } @else {
          <div class="table-wrap">
            <table class="records-table">
              <thead>
                <tr>
                  @for (col of columns; track col.key) {
                    <th [class.sorted]="sort().column === col.key" (click)="onSort(col.key)">
                      <span class="th-content">
                        {{ col.label }}
                        <mat-icon class="sort-icon">{{ sortIcon(col.key) }}</mat-icon>
                      </span>
                    </th>
                  }
                </tr>
              </thead>
              <tbody>
                @for (row of paged(); track $index) {
                  <tr>
                    <td class="mono">{{ row.employeeNumber }}</td>
                    <td>{{ row.employeeName }}</td>
                    <td class="mono">{{ row.serverTime | date: 'yyyy-MM-dd HH:mm:ss' }}</td>
                    <td>{{ eventLabel(row.eventType) }}</td>
                    <td>
                      <span
                        class="status-chip"
                        [class]="statusTone(row.status)"
                        [matTooltip]="rejectionTooltip(row)"
                        [matTooltipDisabled]="!rejectionTooltip(row)"
                      >
                        {{ statusLabels[row.status] }}
                      </span>
                    </td>
                    <td>{{ row.workCenter }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <mat-paginator
            [length]="filtered().length"
            [pageSize]="pageSize()"
            [pageIndex]="pageIndex()"
            [pageSizeOptions]="[25, 50, 100]"
            (page)="onPage($event)"
          />
        }
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      .toolbar {
        display: flex;
        gap: var(--sp-3);
        align-items: flex-start;
        flex-wrap: wrap;
        margin-bottom: var(--sp-2);
      }
      .toolbar .search { flex: 1 1 320px; }
      .dates { display: flex; gap: var(--sp-2); }
      .date { width: 160px; }
      .filter { width: 180px; }

      .indicators { margin: var(--sp-2) 0 var(--sp-3); }
      .count { font-size: 0.9rem; }
      .count strong { font-size: 1.05rem; color: var(--brand); }
      .loader-bar { margin: var(--sp-2) 0; }

      .records-table {
        width: 100%;
        border-collapse: separate;
        border-spacing: 0;
        font-size: 0.85rem;
        min-width: 820px;
      }
      thead th {
        position: sticky;
        top: 0;
        z-index: 2;
        background: var(--surface-2);
        color: var(--text-muted);
        font-size: 0.72rem;
        font-weight: 650;
        text-transform: uppercase;
        letter-spacing: 0.04em;
        text-align: left;
        padding: var(--sp-3) var(--sp-3);
        border-bottom: 1px solid var(--border);
        cursor: pointer;
        user-select: none;
        white-space: nowrap;
        transition: color 0.12s ease;
      }
      thead th:hover { color: var(--text); }
      thead th.sorted { color: var(--brand); }
      .th-content { display: inline-flex; align-items: center; gap: 2px; }
      .sort-icon { font-size: 16px; width: 16px; height: 16px; opacity: 0.7; }
      tbody td {
        padding: var(--sp-3) var(--sp-3);
        border-bottom: 1px solid var(--border);
        color: var(--text);
        white-space: nowrap;
      }
      tbody td.mono { font-variant-numeric: tabular-nums; color: var(--text-muted); }
      tbody tr { transition: background 0.12s ease; animation: fade-in 0.18s ease; }
      tbody tr:nth-child(even) { background: color-mix(in srgb, var(--surface-2) 55%, transparent); }
      tbody tr:hover { background: var(--surface-2); }

      .error-block {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: var(--sp-3);
        padding: 48px 16px;
        color: var(--text-muted);
        text-align: center;
      }
      .error-block mat-icon { font-size: 44px; width: 44px; height: 44px; opacity: 0.6; }
      .error-block p { margin: 0; max-width: 420px; }

      .skeleton { display: flex; flex-direction: column; gap: 10px; padding: var(--sp-4) 0; }
      .skeleton-row {
        height: 40px;
        border-radius: var(--radius-sm);
        background: linear-gradient(90deg, var(--surface-2) 25%, var(--border) 37%, var(--surface-2) 63%);
        background-size: 400% 100%;
        animation: shimmer 1.4s ease infinite;
      }
      @keyframes shimmer {
        0% { background-position: 100% 50%; }
        100% { background-position: 0 50%; }
      }
      @keyframes fade-in {
        from { opacity: 0; }
        to { opacity: 1; }
      }

      mat-paginator { background: transparent; margin-top: var(--sp-2); }
    `,
  ],
})
export class AttendanceRecordsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AttendanceRecordsService);
  private readonly eventTypes = inject(EventTypesService);
  private readonly notify = inject(NotificationService);

  protected readonly skeletonRows = Array.from({ length: 8 }, (_, i) => i);
  protected readonly columns = COLUMNS;
  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly eventTypeOptions = Object.keys(DEFAULT_EVENT_TYPE_LABELS) as AttendanceEventType[];

  protected readonly form: FormGroup = this.buildForm();

  private readonly rawRows = signal<AttendanceRecordReport[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Etiquetas de tipo de evento por empresa (sobre las de respaldo). */
  private readonly eventLabels = signal<Record<string, string>>({ ...DEFAULT_EVENT_TYPE_LABELS });

  protected readonly sort = signal<RecordSortState>({ column: 'serverTime', direction: 'desc' });
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(25);

  private readonly formValue = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  protected readonly total = computed(() => this.rawRows().length);

  protected readonly filtered = computed(() => {
    const f = this.formValue() as Record<string, string>;
    const search = (f['search'] ?? '').trim().toLowerCase();
    return this.rawRows().filter((row) => {
      if (f['status'] !== 'ALL' && row.status !== f['status']) return false;
      if (f['eventType'] !== 'ALL' && row.eventType !== f['eventType']) return false;
      if (search) {
        const haystack = `${row.employeeNumber} ${row.employeeName} ${row.workCenter}`.toLowerCase();
        if (!haystack.includes(search)) return false;
      }
      return true;
    });
  });

  protected readonly sorted = computed(() => {
    const s = this.sort();
    const rows = [...this.filtered()];
    const dir = s.direction === 'asc' ? 1 : -1;
    rows.sort((a, b) => String(a[s.column]).localeCompare(String(b[s.column]), 'es', { numeric: true }) * dir);
    return rows;
  });

  protected readonly paged = computed(() => {
    const size = this.pageSize();
    const rows = this.sorted();
    let start = this.pageIndex() * size;
    if (start >= rows.length) {
      start = 0;
    }
    return rows.slice(start, start + size);
  });

  constructor() {
    const from$ = this.form.get('from')!.valueChanges;
    const to$ = this.form.get('to')!.valueChanges;
    from$.pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed()).subscribe(() => this.reload());
    to$.pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed()).subscribe(() => this.reload());
    this.form.valueChanges.pipe(debounceTime(150), takeUntilDestroyed()).subscribe(() => this.pageIndex.set(0));

    this.loadEventTypeLabels();
    this.reload();
  }

  protected reload(): void {
    const { from, to } = this.form.getRawValue() as { from: string; to: string };
    this.loading.set(true);
    this.error.set(null);
    this.service.records(from, to).subscribe({
      next: (rows) => {
        this.rawRows.set(rows);
        this.loading.set(false);
      },
      error: () => {
        this.rawRows.set([]);
        this.error.set('No se pudo cargar el reporte de registros de asistencia.');
        this.loading.set(false);
        this.notify.error('No se pudo cargar el reporte de registros de asistencia.');
      },
    });
  }

  /** Traduce un código de tipo de evento a su etiqueta (empresa o respaldo). */
  protected eventLabel(code: string): string {
    return this.eventLabels()[code] ?? code;
  }

  protected statusTone(status: AttendanceRecordStatus): string {
    return STATUS_TONES[status];
  }

  protected rejectionTooltip(row: AttendanceRecordReport): string {
    if (!row.rejectionReason) {
      return '';
    }
    return REJECTION_LABELS[row.rejectionReason] ?? row.rejectionReason;
  }

  protected onSort(column: RecordSortColumn): void {
    const current = this.sort();
    if (current.column === column) {
      this.sort.set({ column, direction: current.direction === 'asc' ? 'desc' : 'asc' });
    } else {
      this.sort.set({ column, direction: 'asc' });
    }
  }

  protected onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected sortIcon(column: RecordSortColumn): string {
    if (this.sort().column !== column) {
      return 'unfold_more';
    }
    return this.sort().direction === 'asc' ? 'arrow_upward' : 'arrow_downward';
  }

  /** Carga las etiquetas configuradas por la empresa; si falla, conserva las de respaldo. */
  private loadEventTypeLabels(): void {
    this.eventTypes.list().subscribe({
      next: (settings) => {
        const map: Record<string, string> = { ...DEFAULT_EVENT_TYPE_LABELS };
        for (const s of settings) {
          if (s.label?.trim()) {
            map[s.eventType] = s.label;
          }
        }
        this.eventLabels.set(map);
      },
      error: () => void 0,
    });
  }

  private buildForm(): FormGroup {
    const today = new Date();
    const from = new Date(today);
    from.setDate(from.getDate() - 29);
    return this.fb.group({
      from: this.fb.nonNullable.control(isoDay(from)),
      to: this.fb.nonNullable.control(isoDay(today)),
      search: this.fb.nonNullable.control(''),
      status: this.fb.nonNullable.control('ALL'),
      eventType: this.fb.nonNullable.control('ALL'),
    });
  }
}
