import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AttendanceReport, LATE_THRESHOLD, SortColumn, SortState } from './report.models';

interface ColumnDef {
  key: SortColumn;
  label: string;
  /** Tooltip cuando el encabezado necesita aclaración. */
  hint?: string;
  numeric: boolean;
}

const COLUMNS: readonly ColumnDef[] = [
  { key: 'employeeNumber', label: 'N.º empleado', hint: 'Código de empleado', numeric: false },
  { key: 'employeeName', label: 'Nombre', numeric: false },
  { key: 'workCenter', label: 'Centro de trabajo', numeric: false },
  { key: 'expectedDays', label: 'Días esp.', hint: 'Días laborables esperados', numeric: true },
  { key: 'attendedDays', label: 'Días asist.', hint: 'Días con asistencia registrada', numeric: true },
  { key: 'justifiedAbsences', label: 'Faltas just.', hint: 'Faltas justificadas', numeric: true },
  { key: 'unjustifiedAbsences', label: 'Faltas inj.', hint: 'Faltas injustificadas', numeric: true },
  { key: 'lateArrivals', label: 'Retardos', numeric: true },
  { key: 'workedHours', label: 'H. trabajadas', hint: 'Horas normales laboradas', numeric: true },
  { key: 'overtimeHours', label: 'H. extra', hint: 'Horas extra laboradas', numeric: true },
  { key: 'totalHours', label: 'H. totales', hint: 'Trabajadas + extra', numeric: true },
  { key: 'active', label: 'Estado', numeric: false },
  { key: 'compliancePercentage', label: '% Cumpl.', hint: 'Cumplimiento vs. días esperados', numeric: true },
];

/**
 * Tabla presentacional del reporte (OnPush + trackBy). Encabezado fijo, zebra, hover, badges e
 * indicadores por color. Emite {@code sortChange} al hacer clic en un encabezado.
 */
@Component({
  selector: 'app-report-table',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, MatIconModule, MatTooltipModule],
  template: `
    <div class="table-wrap">
      <table class="report-table">
        <thead>
          <tr>
            @for (col of columns; track col.key) {
              <th
                [class.numeric]="col.numeric"
                [class.sorted]="sort().column === col.key"
                (click)="requestSort(col.key)"
                [matTooltip]="col.hint ?? ''"
                [matTooltipDisabled]="!col.hint"
              >
                <span class="th-content">
                  {{ col.label }}
                  <mat-icon class="sort-icon">{{ sortIcon(col.key) }}</mat-icon>
                </span>
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
        padding: var(--sp-3) var(--sp-3);
        border-bottom: 1px solid var(--border);
        cursor: pointer;
        user-select: none;
        white-space: nowrap;
        transition: color 0.12s ease;
      }
      thead th:hover { color: var(--text); }
      thead th.sorted { color: var(--brand); }
      thead th.numeric { text-align: right; }
      .th-content { display: inline-flex; align-items: center; gap: 2px; }
      th.numeric .th-content { flex-direction: row-reverse; }
      .sort-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        opacity: 0.7;
      }
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
  readonly sortChange = output<SortColumn>();

  protected readonly columns = COLUMNS;
  protected readonly lateThreshold = computed(() => LATE_THRESHOLD);

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
