import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

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
 * Contenido de filtros avanzados (reutilizable, presentacional). Vive dentro del drawer de
 * app-reports; el {@link FormGroup} que recibe del padre es la única fuente de verdad.
 */
@Component({
  selector: 'app-report-filters',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <div [formGroup]="form()">
      <div class="detail-section-title">Datos del colaborador</div>
      <div class="filters-grid">
        <mat-form-field appearance="outline" class="drawer-field">
          <mat-label>N.º empleado</mat-label>
          <input matInput formControlName="employeeNumber" autocomplete="off" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="drawer-field">
          <mat-label>Nombre</mat-label>
          <input matInput formControlName="employeeName" autocomplete="off" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="drawer-field">
          <mat-label>Centro de trabajo</mat-label>
          <input matInput formControlName="workCenter" autocomplete="off" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="drawer-field">
          <mat-label>Estado</mat-label>
          <mat-select formControlName="status">
            <mat-option value="ALL">Todos</mat-option>
            <mat-option value="ACTIVE">Activos</mat-option>
            <mat-option value="INACTIVE">Inactivos</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="detail-section-title">Rangos numéricos</div>
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
    </div>
  `,
  styles: [
    `
      .filters-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: var(--sp-1);
        margin-bottom: var(--sp-2);
      }
      .ranges-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: var(--sp-3);
      }
      .range-item { display: flex; flex-direction: column; gap: 4px; }
      .range-label { font-size: var(--font-small); font-weight: 600; color: var(--text-muted); }
      .range-inputs { display: flex; gap: var(--sp-2); }
      .range-field { flex: 1; }
      mat-form-field { width: 100%; }
    `,
  ],
})
export class ReportFiltersComponent {
  /** FormGroup provisto por el padre (fuente de verdad). */
  readonly form = input.required<FormGroup>();

  protected readonly rangeFields = RANGE_FIELDS;
}
