import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../../core/ui/empty-state.component';
import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { StatusChipComponent } from '../../../core/ui/status-chip.component';
import { Project } from './project.models';
import { ProjectService } from './project.service';

/** Catálogo de proyectos (RF-23). Alta y edición viven en páginas propias con breadcrumb. */
@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    PageHeaderComponent,
    StatusChipComponent,
    EmptyStateComponent,
  ],
  template: `
    <app-page-header title="Proyectos" subtitle="Administra los proyectos de la organización">
      <button mat-flat-button color="primary" (click)="router.navigate(['/projects/new'])">
        <mat-icon>add</mat-icon> Nuevo proyecto
      </button>
    </app-page-header>

    <mat-card>
      <mat-card-content>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <div class="table-wrap">
          <table mat-table [dataSource]="projects()" style="width:100%">
            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>Código</th>
              <td mat-cell *matCellDef="let p">{{ p.code }}</td>
            </ng-container>
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Nombre</th>
              <td mat-cell *matCellDef="let p">{{ p.name }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Estado</th>
              <td mat-cell *matCellDef="let p"><app-status-chip [status]="p.status" /></td>
            </ng-container>
            <ng-container matColumnDef="dates">
              <th mat-header-cell *matHeaderCellDef>Vigencia</th>
              <td mat-cell *matCellDef="let p">{{ p.startsOn || '—' }} → {{ p.endsOn || '—' }}</td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let p" style="text-align:right">
                <button mat-icon-button aria-label="Editar"><mat-icon>chevron_right</mat-icon></button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns" (click)="edit(row)" style="cursor:pointer"></tr>
          </table>
        </div>

        @if (!loading() && projects().length === 0) {
          <app-empty-state icon="work" message="No hay proyectos para mostrar." />
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
export class ProjectsComponent {
  private readonly service = inject(ProjectService);
  protected readonly router = inject(Router);

  protected readonly columns = ['code', 'name', 'status', 'dates', 'actions'];
  protected readonly projects = signal<Project[]>([]);
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
        this.projects.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el listado (¿permiso project:manage?).');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }

  protected edit(project: Project): void {
    void this.router.navigate(['/projects', project.id, 'edit']);
  }
}
