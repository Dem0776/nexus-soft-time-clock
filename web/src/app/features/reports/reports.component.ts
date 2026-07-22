import { HttpResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';

import { NotificationService } from '../../core/ui/notification.service';
import { PageHeaderComponent } from '../../core/ui/page-header.component';
import { ReportService } from './report.service';

/** Reportes de asistencia (RF-11): filtros avanzados y exportación a CSV/Excel/PDF. */
@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressBarModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header title="Reportes de asistencia" />
    <mat-card>
      <mat-card-content>
        <p class="muted">Filtrá y exportá el reporte de asistencia. La descarga inicia automáticamente.</p>
        <form [formGroup]="form" (ngSubmit)="download()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:baseline">
          <mat-form-field appearance="outline" style="width:170px">
            <mat-label>Desde</mat-label>
            <input matInput type="date" formControlName="from" />
          </mat-form-field>
          <mat-form-field appearance="outline" style="width:170px">
            <mat-label>Hasta</mat-label>
            <input matInput type="date" formControlName="to" />
          </mat-form-field>
          <mat-form-field appearance="outline" style="width:200px">
            <mat-label>Estado</mat-label>
            <mat-select formControlName="status">
              <mat-option [value]="''">Todos</mat-option>
              <mat-option value="ACCEPTED">Aceptados</mat-option>
              <mat-option value="REJECTED">Rechazados</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" style="width:150px">
            <mat-label>Formato</mat-label>
            <mat-select formControlName="format">
              <mat-option value="csv">CSV</mat-option>
              <mat-option value="xlsx">Excel</mat-option>
              <mat-option value="pdf">PDF</mat-option>
            </mat-select>
          </mat-form-field>
          <button mat-flat-button color="primary" type="submit" [disabled]="loading()">
            <mat-icon>download</mat-icon> Exportar
          </button>
        </form>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      </mat-card-content>
    </mat-card>
  `,
})
export class ReportsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ReportService);
  private readonly notify = inject(NotificationService);

  protected readonly loading = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    from: [''],
    to: [''],
    status: [''],
    format: ['csv'],
  });

  protected download(): void {
    const raw = this.form.getRawValue();
    this.loading.set(true);
    this.service
      .attendance({
        from: raw.from ? `${raw.from}T00:00:00Z` : undefined,
        to: raw.to ? `${raw.to}T23:59:59Z` : undefined,
        status: raw.status || undefined,
        format: raw.format as 'csv' | 'xlsx' | 'pdf',
      })
      .subscribe({
        next: (response) => {
          this.saveFile(response);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.notify.error('No se pudo generar el reporte (¿permiso report:export?).');
        },
      });
  }

  private saveFile(response: HttpResponse<Blob>): void {
    const body = response.body;
    if (!body) {
      return;
    }
    const filename = this.filenameFrom(response) ?? `asistencia.${this.form.getRawValue().format}`;
    const url = URL.createObjectURL(body);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }

  private filenameFrom(response: HttpResponse<Blob>): string | null {
    const disposition = response.headers.get('content-disposition');
    const match = disposition?.match(/filename="?([^"]+)"?/i);
    return match ? match[1] : null;
  }
}
