import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';

/** Definición declarativa de un filtro de rango numérico (etiqueta + nombre del subgrupo). */
interface RangeField {
  key: string;
  label: string;
}

const RANGE_FIELDS: readonly RangeField[] = [
  { key: 'expectedDays', label: 'Días esperados' },
  { key: 'attendedDays', label: 'Días asistidos' },
  { key: 'justifiedAbsences', label: 'Faltas justificadas' },
  { key: 'unjustifiedAbsences', label: 'Faltas injustificadas' },
  { key: 'lateArrivals', label: 'Retardos' },
  { key: 'workedHours', label: 'Horas trabajadas' },
  { key: 'overtimeHours', label: 'Horas extra' },
  { key: 'totalHours', label: 'Horas totales' },
  { key: 'compliancePercentage', label: '% Cumplimiento' },
];

/**
 * Panel de filtros avanzados (reutilizable, presentacional). Renderiza controles ligados al
 * {@link FormGroup} que le pasa el padre — este último es la única fuente de verdad del estado.
 */
@Component({
  selector: 'app-report-filters',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  template: `
    <mat-expansion-panel [formGroup]="form()" class="filters-panel" [expanded]="expanded()">
      <mat-expansion-panel-header>
        <mat-panel-title>
          <mat-icon>tune</mat-icon> Filtros avanzados
        </mat-panel-title>
        <mat-panel-description>
          @if (activeCount() > 0) {
            <span class="active-badge">{{ activeCount() }} activo{{ activeCount() === 1 ? '' : 's' }}</span>
          }
        </mat-panel-description>
      </mat-expansion-panel-header>

      <div class="filters-grid">
        <mat-form-field appearance="outline">
          <mat-label>N.º empleado</mat-label>
          <input matInput formControlName="employeeNumber" autocomplete="off" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Nombre</mat-label>
          <input matInput formControlName="employeeName" autocomplete="off" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Centro de trabajo</mat-label>
          <input matInput formControlName="workCenter" autocomplete="off" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Estado</mat-label>
          <mat-select formControlName="status">
            <mat-option value="ALL">Todos</mat-option>
            <mat-option value="ACTIVE">Activos</mat-option>
            <mat-option value="INACTIVE">Inactivos</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="ranges-grid" formGroupName="ranges">
        @for (f of rangeFields; track f.key) {
          <div class="range-item" [formGroupName]="f.key">
            <span class="range-label">{{ f.label }}</span>
            <div class="range-inputs">
              <mat-form-field appearance="outline" class="range-field">
                <mat-label>Mín</mat-label>
                <input matInput type="number" formControlName="min" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="range-field">
                <mat-label>Máx</mat-label>
                <input matInput type="number" formControlName="max" />
              </mat-form-field>
            </div>
          </div>
        }
      </div>
    </mat-expansion-panel>
  `,
  styles: [
    `
      .filters-panel {
        background: var(--surface) !important;
        border: 1px solid var(--border);
        border-radius: var(--radius-md) !important;
        box-shadow: none !important;
      }
      mat-panel-title {
        display: flex;
        align-items: center;
        gap: var(--sp-2);
        font-weight: 600;
      }
      .active-badge {
        color: var(--info);
        background: var(--info-bg);
        padding: 2px 10px;
        border-radius: 999px;
        font-size: 0.75rem;
        font-weight: 600;
      }
      .filters-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: var(--sp-3);
        margin-bottom: var(--sp-3);
      }
      .ranges-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: var(--sp-3);
      }
      .range-item {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }
      .range-label {
        font-size: 0.78rem;
        font-weight: 600;
        color: var(--text-muted);
      }
      .range-inputs {
        display: flex;
        gap: var(--sp-2);
      }
      .range-field {
        flex: 1;
      }
      mat-form-field {
        width: 100%;
      }
    `,
  ],
})
export class ReportFiltersComponent {
  /** FormGroup provisto por el padre (fuente de verdad). */
  readonly form = input.required<FormGroup>();
  /** Cantidad de filtros activos (para el badge del encabezado). */
  readonly activeCount = input(0);
  /** Si el panel arranca expandido. */
  readonly expanded = input(false);

  protected readonly rangeFields = RANGE_FIELDS;
}
