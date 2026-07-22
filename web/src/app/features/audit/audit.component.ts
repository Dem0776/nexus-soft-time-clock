import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../core/ui/page-header.component';
import { AuditEntry } from './audit.models';
import { AuditService } from './audit.service';

/**
 * Bitácora de auditoría (RF-12): consulta paginada de solo lectura. Los registros son
 * inmutables (RN-61): la UI no ofrece editar ni borrar.
 */
@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [MatCardModule, MatTableModule, MatPaginatorModule, MatProgressBarModule, PageHeaderComponent, EmptyStateComponent],
  template: `
    <app-page-header title="Auditoría" />
    <mat-card>
      <mat-card-content>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <table mat-table [dataSource]="entries()" style="width:100%">
          <ng-container matColumnDef="createdAt">
            <th mat-header-cell *matHeaderCellDef>Fecha/hora</th>
            <td mat-cell *matCellDef="let e">{{ e.createdAt }}</td>
          </ng-container>
          <ng-container matColumnDef="actorUserId">
            <th mat-header-cell *matHeaderCellDef>Usuario</th>
            <td mat-cell *matCellDef="let e">{{ e.actorUserId }}</td>
          </ng-container>
          <ng-container matColumnDef="action">
            <th mat-header-cell *matHeaderCellDef>Acción</th>
            <td mat-cell *matCellDef="let e">{{ e.action }}</td>
          </ng-container>
          <ng-container matColumnDef="resource">
            <th mat-header-cell *matHeaderCellDef>Recurso</th>
            <td mat-cell *matCellDef="let e">{{ e.resourceType }} · {{ e.resourceId }}</td>
          </ng-container>
          <ng-container matColumnDef="newValues">
            <th mat-header-cell *matHeaderCellDef>Valores nuevos</th>
            <td mat-cell *matCellDef="let e">
              <code style="font-size:.75rem">{{ e.newValues || '—' }}</code>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>

        @if (!loading() && entries().length === 0) {
          <app-empty-state icon="fact_check" message="No hay registros de auditoría." />
        }

        <mat-paginator
          [length]="total()"
          [pageSize]="size()"
          [pageIndex]="page()"
          [pageSizeOptions]="[25, 50, 100]"
          (page)="onPage($event)"
        />
      </mat-card-content>
    </mat-card>
  `,
})
export class AuditComponent {
  private readonly service = inject(AuditService);

  protected readonly columns = ['createdAt', 'actorUserId', 'action', 'resource', 'newValues'];
  protected readonly entries = signal<AuditEntry[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(50);
  protected readonly total = signal(0);

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(this.page(), this.size()).subscribe({
      next: (result) => {
        this.entries.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la bitácora (¿permiso audit:read?).');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }
}
