import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../core/ui/empty-state.component';
import { NotificationService } from '../../core/ui/notification.service';
import { PageHeaderComponent } from '../../core/ui/page-header.component';
import { StatusChipComponent } from '../../core/ui/status-chip.component';
import { Incident } from './incident.models';
import { IncidentService } from './incident.service';
import { ResolveIncidentDialogComponent } from './resolve-incident-dialog.component';

/** Bandeja de incidencias (RF-09, CU-08): filtrar por estado y resolver con comentario. */
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
    MatSelectModule,
    MatProgressBarModule,
    MatDialogModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Incidencias" />
    <mat-card>
      <mat-card-content>
        <mat-form-field appearance="outline" style="width:220px">
          <mat-label>Estado</mat-label>
          <mat-select [formControl]="statusFilter" (selectionChange)="applyFilter()">
            <mat-option [value]="''">Todas</mat-option>
            <mat-option value="OPEN">Abiertas</mat-option>
            <mat-option value="APPROVED">Aprobadas</mat-option>
            <mat-option value="REJECTED">Rechazadas</mat-option>
            <mat-option value="RESOLVED">Resueltas</mat-option>
          </mat-select>
        </mat-form-field>

        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <table mat-table [dataSource]="incidents()" style="width:100%">
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Fecha</th>
            <td mat-cell *matCellDef="let i">{{ i.incidentDate }}</td>
          </ng-container>
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Tipo</th>
            <td mat-cell *matCellDef="let i">{{ i.type }}</td>
          </ng-container>
          <ng-container matColumnDef="priority">
            <th mat-header-cell *matHeaderCellDef>Prioridad</th>
            <td mat-cell *matCellDef="let i">{{ i.priority }}</td>
          </ng-container>
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Detalle</th>
            <td mat-cell *matCellDef="let i">{{ i.description || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Estado</th>
            <td mat-cell *matCellDef="let i"><app-status-chip [status]="i.status" /></td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let i" style="text-align:right">
              <button mat-stroked-button (click)="resolve(i)" [disabled]="i.status !== 'OPEN'">Resolver</button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>

        @if (!loading() && incidents().length === 0) {
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
  `,
})
export class IncidentsComponent {
  private readonly service = inject(IncidentService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['date', 'type', 'priority', 'description', 'status', 'actions'];
  protected readonly incidents = signal<Incident[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  protected readonly statusFilter = new FormControl('', { nonNullable: true });

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

  protected applyFilter(): void {
    this.page.set(0);
    this.reload();
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected resolve(incident: Incident): void {
    this.dialog
      .open(ResolveIncidentDialogComponent, { data: incident })
      .afterClosed()
      .subscribe((result) => {
        if (!result) {
          return;
        }
        this.service.resolve(incident.id, result).subscribe({
          next: () => {
            this.notify.success('Incidencia resuelta.');
            this.reload();
          },
          error: () => this.notify.error('No se pudo resolver la incidencia.'),
        });
      });
  }
}
