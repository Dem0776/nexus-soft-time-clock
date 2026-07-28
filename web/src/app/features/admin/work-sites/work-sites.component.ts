import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ConfirmDialogComponent } from '../../../core/ui/confirm-dialog.component';
import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { NotificationService } from '../../../core/ui/notification.service';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { WorkSite } from './work-site.models';
import { WorkSiteService } from './work-site.service';

/** Catálogo de centros de trabajo (RF-07). Alta y edición viven en páginas propias con breadcrumb. */
@Component({
  selector: 'app-work-sites',
  standalone: true,
  imports: [
    DecimalPipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatTableModule,
    MatPaginatorModule,
    MatDialogModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Centros de trabajo" subtitle="Administra los centros de trabajo de la empresa">
      <button mat-flat-button color="primary" (click)="router.navigate(['/work-sites/new'])">
        <mat-icon>add_location_alt</mat-icon> Nuevo centro
      </button>
    </app-page-header>

    <mat-card>
      <mat-card-content>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <div class="table-wrap">
          <table mat-table [dataSource]="sites()" style="width:100%">
            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>Código</th>
              <td mat-cell *matCellDef="let s">{{ s.code }}</td>
            </ng-container>
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Nombre</th>
              <td mat-cell *matCellDef="let s">{{ s.name }}</td>
            </ng-container>
            <ng-container matColumnDef="location">
              <th mat-header-cell *matHeaderCellDef>Ubicación</th>
              <td mat-cell *matCellDef="let s">{{ s.latitude | number: '1.4-6' }}, {{ s.longitude | number: '1.4-6' }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Estado</th>
              <td mat-cell *matCellDef="let s"><app-status-chip [status]="s.status" /></td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let s" style="text-align:right;white-space:nowrap">
                <a mat-icon-button [routerLink]="['/work-sites', s.id, 'geofence']" matTooltip="Geocerca y QR" (click)="$event.stopPropagation()">
                  <mat-icon>my_location</mat-icon>
                </a>
                @if (s.status === 'ACTIVE') {
                  <button mat-icon-button (click)="toggleStatus(s); $event.stopPropagation()" matTooltip="Desactivar"><mat-icon>block</mat-icon></button>
                } @else {
                  <button mat-icon-button (click)="toggleStatus(s); $event.stopPropagation()" matTooltip="Activar"><mat-icon>check_circle</mat-icon></button>
                }
                <button mat-icon-button (click)="edit(s)" aria-label="Editar"><mat-icon>chevron_right</mat-icon></button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns" (click)="edit(row)" style="cursor:pointer"></tr>
          </table>
        </div>

        @if (!loading() && sites().length === 0) {
          <app-empty-state icon="place" message="No hay centros de trabajo para mostrar." />
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
export class WorkSitesComponent {
  private readonly service = inject(WorkSiteService);
  private readonly notify = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  protected readonly router = inject(Router);

  protected readonly columns = ['code', 'name', 'location', 'status', 'actions'];
  protected readonly sites = signal<WorkSite[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(this.page(), this.size()).subscribe({
      next: (result) => {
        this.sites.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el listado (¿permiso worksite:manage?).');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected edit(site: WorkSite): void {
    void this.router.navigate(['/work-sites', site.id, 'edit']);
  }

  protected toggleStatus(site: WorkSite): void {
    const next = site.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: next === 'INACTIVE' ? 'Desactivar centro' : 'Activar centro',
          message: `¿Confirmás cambiar el estado de "${site.name}" a ${next}?`,
          color: next === 'INACTIVE' ? 'warn' : 'primary',
        },
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.service.setStatus(site.id, next).subscribe({
          next: () => {
            this.notify.success('Estado actualizado.');
            this.reload();
          },
          error: () => this.notify.error('No se pudo cambiar el estado.'),
        });
      });
  }
}
