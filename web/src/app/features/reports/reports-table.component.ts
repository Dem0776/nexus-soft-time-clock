import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AttendanceReport, LATE_THRESHOLD, SortColumn, SortState, StatusFilter } from './report.models';

/** Tipo de filtro por columna (embudo estilo Excel en el encabezado). */
type ColumnFilter = 'text' | 'number' | 'status';

interface ColumnDef {
  key: SortColumn;
  label: string;
  /** Tooltip cuando el encabezado necesita aclaración. */
  hint?: string;
  numeric: boolean;
  /** Tipo de filtro por encabezado; sin valor = columna no filtrable. */
  filter?: ColumnFilter;
  /** Nombre del control (texto) o clave del rango numérico dentro de `ranges`. */
  ctrl?: string;
}

const COLUMNS: readonly ColumnDef[] = [
  { key: 'employeeNumber', label: 'N.º empleado', hint: 'Código de empleado', numeric: false, filter: 'text', ctrl: 'employeeNumber' },
  { key: 'employeeName', label: 'Nombre', numeric: false, filter: 'text', ctrl: 'employeeName' },
  { key: 'workCenter', label: 'Centro de trabajo', numeric: false, filter: 'text', ctrl: 'workCenter' },
  { key: 'expectedDays', label: 'Días esp.', hint: 'Días laborables esperados', numeric: true, filter: 'number', ctrl: 'expectedDays' },
  { key: 'attendedDays', label: 'Días asist.', hint: 'Días con asistencia registrada', numeric: true, filter: 'number', ctrl: 'attendedDays' },
  { key: 'justifiedAbsences', label: 'Faltas just.', hint: 'Faltas justificadas', numeric: true, filter: 'number', ctrl: 'justifiedAbsences' },
  { key: 'unjustifiedAbsences', label: 'Faltas inj.', hint: 'Faltas injustificadas', numeric: true, filter: 'number', ctrl: 'unjustifiedAbsences' },
  { key: 'lateArrivals', label: 'Retardos', numeric: true, filter: 'number', ctrl: 'lateArrivals' },
  { key: 'workedHours', label: 'H. trabajadas', hint: 'Horas normales laboradas', numeric: true, filter: 'number', ctrl: 'workedHours' },
  { key: 'overtimeHours', label: 'H. extra', hint: 'Horas extra laboradas', numeric: true, filter: 'number', ctrl: 'overtimeHours' },
  { key: 'totalHours', label: 'H. totales', hint: 'Trabajadas + extra', numeric: true, filter: 'number', ctrl: 'totalHours' },
  { key: 'active', label: 'Estado', numeric: false, filter: 'status' },
  { key: 'compliancePercentage', label: '% Cumpl.', hint: 'Cumplimiento vs. días esperados', numeric: true, filter: 'number', ctrl: 'compliancePercentage' },
];

/**
 * Tabla presentacional del reporte (OnPush + trackBy). Encabezado fijo con orden por clic e
 * indicadores por color. Cada columna filtrable expone un embudo estilo Excel que abre un popup
 * conectado al FormGroup de filtros del componente padre (texto / rango numérico / estado).
 */
@Component({
  selector: 'app-report-table',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    <div class="table-wrap">
      <table class="report-table">
        <thead>
          <tr>
            @for (col of columns; track col.key) {
              <th
                [class.numeric]="col.numeric"
                [class.sorted]="sort().column === col.key"
                [class.filtered]="isFilterActive(col)"
              >
                <div class="th-inner">
                  <span
                    class="th-label"
                    (click)="requestSort(col.key)"
                    [matTooltip]="col.hint ?? ''"
                    [matTooltipDisabled]="!col.hint"
                  >
                    {{ col.label }}
                    <mat-icon class="sort-icon">{{ sortIcon(col.key) }}</mat-icon>
                  </span>

                  @if (col.filter) {
                    <button
                      type="button"
                      class="filter-btn"
                      [class.on]="isFilterActive(col)"
                      [matMenuTriggerFor]="fmenu"
                      (click)="$event.stopPropagation()"
                      [matTooltip]="'Filtrar por ' + col.label"
                      aria-label="Filtrar columna"
                    >
                      <mat-icon>filter_alt</mat-icon>
                    </button>

                    <mat-menu #fmenu="matMenu">
                      <div
                        style="padding:12px;min-width:210px;display:flex;flex-direction:column;gap:8px"
                        (click)="$event.stopPropagation()"
                        (keydown)="$event.stopPropagation()"
                      >
                        <div style="font-size:.72rem;font-weight:700;text-transform:uppercase;letter-spacing:.04em;opacity:.7">
                          {{ col.label }}
                        </div>

                        @switch (col.filter) {
                          @case ('text') {
                            <mat-form-field appearance="outline" subscriptSizing="dynamic" style="width:100%">
                              <mat-label>Contiene</mat-label>
                              <input matInput [formControl]="textCtrl(col.ctrl!)" placeholder="Escribe para filtrar" autocomplete="off" />
                            </mat-form-field>
                          }
                          @case ('number') {
                            <div style="display:flex;gap:8px">
                              <mat-form-field appearance="outline" subscriptSizing="dynamic" style="width:96px">
                                <mat-label>Mín</mat-label>
                                <input matInput type="number" [formControl]="rangeCtrl(col.ctrl!, 'min')" />
                              </mat-form-field>
                              <mat-form-field appearance="outline" subscriptSizing="dynamic" style="width:96px">
                                <mat-label>Máx</mat-label>
                                <input matInput type="number" [formControl]="rangeCtrl(col.ctrl!, 'max')" />
                              </mat-form-field>
                            </div>
                          }
                          @case ('status') {
                            <div style="display:flex;gap:6px;flex-wrap:wrap">
                              @for (opt of statusOptions; track opt.value) {
                                <button
                                  type="button"
                                  mat-stroked-button
                                  [color]="statusCtrl().value === opt.value ? 'primary' : undefined"
                                  (click)="statusCtrl().setValue(opt.value)"
                                >
                                  {{ opt.label }}
                                </button>
                              }
                            </div>
                          }
                        }

                        <button
                          type="button"
                          mat-button
                          (click)="clearColumn(col)"
                          [disabled]="!isFilterActive(col)"
                          style="align-self:flex-start"
                        >
                          <mat-icon>backspace</mat-icon> Limpiar
                        </button>
                      </div>
                    </mat-menu>
                  }
                </div>
              </th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of rows(); track row.employeeNumber) {
            <tr>
              <td class="mono">{{ row.employeeNumber }}</td>
              <td>{{ row.employeeName }}</td>
              <td>{{ row.workCenter }}</td>
              <td class="numeric">{{ row.expectedDays }}</td>
              <td class="numeric">{{ row.attendedDays }}</td>
              <td class="numeric">
                <span class="badge" [class]="absenceTone(row.justifiedAbsences)"
                  [matTooltip]="row.justifiedAbsences === 0 ? 'Sin faltas justificadas' : row.justifiedAbsences + ' falta(s) justificada(s)'">
                  {{ row.justifiedAbsences }}
                </span>
              </td>
              <td class="numeric">
                <span class="badge" [class]="absenceTone(row.unjustifiedAbsences)"
                  [matTooltip]="row.unjustifiedAbsences === 0 ? 'Sin faltas injustificadas' : row.unjustifiedAbsences + ' falta(s) injustificada(s)'">
                  {{ row.unjustifiedAbsences }}
                </span>
              </td>
              <td class="numeric">
                <span class="badge" [class]="lateTone(row.lateArrivals)"
                  [matTooltip]="lateTooltip(row.lateArrivals)">
                  {{ row.lateArrivals }}
                </span>
              </td>
              <td class="numeric">{{ row.workedHours | number: '1.0-1' }}</td>
              <td class="numeric">{{ row.overtimeHours | number: '1.0-1' }}</td>
              <td class="numeric strong">{{ row.totalHours | number: '1.0-1' }}</td>
              <td>
                <span class="status-chip" [class]="row.active ? 'success' : 'neutral'">
                  {{ row.active ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td class="numeric">
                <span class="badge" [class]="complianceTone(row.compliancePercentage)"
                  [matTooltip]="row.compliancePercentage + '% de cumplimiento'">
                  {{ row.compliancePercentage | number: '1.0-1' }}%
                </span>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: [
    `
      .report-table {
        width: 100%;
        border-collapse: separate;
        border-spacing: 0;
        font-size: 0.85rem;
        min-width: 1100px;
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
        padding: var(--sp-2) var(--sp-3);
        border-bottom: 1px solid var(--border);
        user-select: none;
        white-space: nowrap;
        transition: color 0.12s ease;
      }
      thead th.sorted { color: var(--brand); }
      thead th.filtered { background: color-mix(in srgb, var(--brand) 10%, var(--surface-2)); }
      thead th.numeric { text-align: right; }

      .th-inner { display: inline-flex; align-items: center; gap: 2px; }
      th.numeric .th-inner { flex-direction: row-reverse; }
      .th-label { display: inline-flex; align-items: center; gap: 2px; cursor: pointer; }
      .th-label:hover { color: var(--text); }
      th.numeric .th-label { flex-direction: row-reverse; }
      .sort-icon { font-size: 16px; width: 16px; height: 16px; opacity: 0.7; }

      .filter-btn {
        display: inline-grid;
        place-items: center;
        width: 22px;
        height: 22px;
        padding: 0;
        border: none;
        border-radius: var(--radius-sm);
        background: transparent;
        color: var(--text-soft);
        cursor: pointer;
        opacity: 0.55;
        transition: opacity 0.12s ease, color 0.12s ease, background 0.12s ease;
      }
      .filter-btn:hover { opacity: 1; background: var(--surface); color: var(--text); }
      .filter-btn mat-icon { font-size: 16px; width: 16px; height: 16px; }
      .filter-btn.on { opacity: 1; color: var(--brand); }

      tbody td {
        padding: var(--sp-3) var(--sp-3);
        border-bottom: 1px solid var(--border);
        color: var(--text);
        white-space: nowrap;
      }
      tbody td.numeric { text-align: right; font-variant-numeric: tabular-nums; }
      tbody td.mono { font-variant-numeric: tabular-nums; color: var(--text-muted); }
      tbody td.strong { font-weight: 650; }
      tbody tr { transition: background 0.12s ease; animation: fade-in 0.18s ease; }
      tbody tr:nth-child(even) { background: color-mix(in srgb, var(--surface-2) 55%, transparent); }
      tbody tr:hover { background: var(--surface-2); }

      .badge {
        display: inline-block;
        min-width: 34px;
        padding: 2px 8px;
        border-radius: 999px;
        font-size: 0.78rem;
        font-weight: 650;
        text-align: center;
        font-variant-numeric: tabular-nums;
      }
      .badge.success { color: var(--success); background: var(--success-bg); }
      .badge.warning { color: var(--warning); background: var(--warning-bg); }
      .badge.danger { color: var(--danger); background: var(--danger-bg); }
      .badge.neutral { color: var(--neutral); background: var(--neutral-bg); }

      @keyframes fade-in {
        from { opacity: 0; }
        to { opacity: 1; }
      }
    `,
  ],
})
export class ReportTableComponent {
  readonly rows = input.required<AttendanceReport[]>();
  readonly sort = input.required<SortState>();
  /** FormGroup de filtros del componente padre (mismo estado que alimenta el filtrado). */
  readonly filters = input.required<FormGroup>();
  readonly sortChange = output<SortColumn>();

  protected readonly columns = COLUMNS;
  protected readonly lateThreshold = computed(() => LATE_THRESHOLD);
  protected readonly statusOptions: ReadonlyArray<{ value: StatusFilter; label: string }> = [
    { value: 'ALL', label: 'Todos' },
    { value: 'ACTIVE', label: 'Activo' },
    { value: 'INACTIVE', label: 'Inactivo' },
  ];

  // --- Acceso a los controles del formulario de filtros del padre ---

  protected textCtrl(name: string): FormControl {
    return this.filters().get(name) as FormControl;
  }

  protected rangeCtrl(key: string, bound: 'min' | 'max'): FormControl {
    return this.filters().get(['ranges', key, bound]) as FormControl;
  }

  protected statusCtrl(): FormControl {
    return this.filters().get('status') as FormControl;
  }

  /** ¿La columna tiene un filtro con valor? (para pintar el embudo activo). */
  protected isFilterActive(col: ColumnDef): boolean {
    if (col.filter === 'text' && col.ctrl) {
      return !!(this.textCtrl(col.ctrl).value as string)?.trim();
    }
    if (col.filter === 'status') {
      return this.statusCtrl().value !== 'ALL';
    }
    if (col.filter === 'number' && col.ctrl) {
      return this.rangeCtrl(col.ctrl, 'min').value != null || this.rangeCtrl(col.ctrl, 'max').value != null;
    }
    return false;
  }

  protected clearColumn(col: ColumnDef): void {
    if (col.filter === 'text' && col.ctrl) {
      this.textCtrl(col.ctrl).setValue('');
    } else if (col.filter === 'status') {
      this.statusCtrl().setValue('ALL');
    } else if (col.filter === 'number' && col.ctrl) {
      this.rangeCtrl(col.ctrl, 'min').setValue(null);
      this.rangeCtrl(col.ctrl, 'max').setValue(null);
    }
  }

  protected requestSort(column: SortColumn): void {
    this.sortChange.emit(column);
  }

  protected sortIcon(column: SortColumn): string {
    if (this.sort().column !== column) {
      return 'unfold_more';
    }
    return this.sort().direction === 'asc' ? 'arrow_upward' : 'arrow_downward';
  }

  protected absenceTone(value: number): string {
    return value === 0 ? 'success' : 'danger';
  }

  protected lateTone(value: number): string {
    if (value === 0) {
      return 'neutral';
    }
    return value >= LATE_THRESHOLD ? 'danger' : 'warning';
  }

  protected lateTooltip(value: number): string {
    if (value === 0) {
      return 'Sin retardos';
    }
    return value >= LATE_THRESHOLD ? `${value} retardos (supera el umbral de ${LATE_THRESHOLD})` : `${value} retardo(s)`;
  }

  protected complianceTone(value: number): string {
    if (value >= 95) {
      return 'success';
    }
    return value >= 80 ? 'warning' : 'danger';
  }
}
