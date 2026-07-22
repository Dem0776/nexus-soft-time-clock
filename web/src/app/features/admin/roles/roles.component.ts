import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';

import { PageHeaderComponent } from '../../../core/ui/page-header.component';
import { Role } from './role.models';
import { RoleService } from './role.service';

/**
 * Catálogo de roles del sistema (RF-22). El backend expone las plantillas de rol
 * (código y nombre); la matriz de permisos granular por tenant se aborda en una
 * iteración posterior del backend.
 */
@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [MatCardModule, MatTableModule, MatProgressBarModule, PageHeaderComponent],
  template: `
    <app-page-header title="Roles" />
    <mat-card>
      <mat-card-content>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }

        <table mat-table [dataSource]="roles()" style="width:100%">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Código</th>
            <td mat-cell *matCellDef="let r"><code>{{ r.code }}</code></td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let r">{{ r.name }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </mat-card-content>
    </mat-card>
  `,
})
export class RolesComponent {
  private readonly service = inject(RoleService);

  protected readonly columns = ['code', 'name'];
  protected readonly roles = signal<Role[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.loading.set(true);
    this.service.list().subscribe({
      next: (roles) => {
        this.roles.set(roles);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo de roles (¿permiso role:manage?).');
        this.loading.set(false);
      },
    });
  }
}
