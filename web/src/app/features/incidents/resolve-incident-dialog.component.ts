import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { INCIDENT_RESOLUTIONS, Incident, ResolveIncident } from './incident.models';

/** Resolución de una incidencia con estado y comentario (CU-08). Devuelve {@link ResolveIncident} o null. */
@Component({
  selector: 'app-resolve-incident-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>Resolver incidencia</h2>
    <mat-dialog-content>
      <p class="muted">{{ data.type }} · {{ data.incidentDate }}</p>
      @if (data.description) { <p>{{ data.description }}</p> }
      <form [formGroup]="form" style="display:flex;flex-direction:column;gap:8px;min-width:320px">
        <mat-form-field appearance="outline">
          <mat-label>Resolución</mat-label>
          <mat-select formControlName="status">
            @for (r of resolutions; track r) { <mat-option [value]="r">{{ r }}</mat-option> }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Comentario</mat-label>
          <textarea matInput formControlName="note" rows="3"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="close()">Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="save()">Guardar</button>
    </mat-dialog-actions>
  `,
})
export class ResolveIncidentDialogComponent {
  protected readonly data = inject<Incident>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<ResolveIncidentDialogComponent, ResolveIncident | null>);
  private readonly fb = inject(FormBuilder);

  protected readonly resolutions = INCIDENT_RESOLUTIONS;
  protected readonly form = this.fb.nonNullable.group({
    status: ['APPROVED', [Validators.required]],
    note: ['', [Validators.required]],
  });

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    this.ref.close({ status: raw.status as ResolveIncident['status'], note: raw.note });
  }

  protected close(): void {
    this.ref.close(null);
  }
}
