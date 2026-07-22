import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

import { Role } from '../roles/role.models';

export interface AssignRolesData {
  userLabel: string;
  /** Roles asignables (ya filtrados para no exceder el privilegio del operador, HU-21 CA1). */
  assignableRoles: Role[];
  /** Códigos de rol actualmente asignados al usuario. */
  selected: string[];
}

/** Diálogo para asignar roles a un usuario (RF-22). Devuelve los códigos seleccionados o null. */
@Component({
  selector: 'app-assign-roles-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatCheckboxModule],
  template: `
    <h2 mat-dialog-title>Roles de {{ data.userLabel }}</h2>
    <mat-dialog-content>
      @if (data.assignableRoles.length === 0) {
        <p class="muted">No tenés roles asignables (no podés otorgar roles de mayor privilegio que el propio).</p>
      }
      <div style="display:flex;flex-direction:column;gap:8px">
        @for (role of data.assignableRoles; track role.code) {
          <mat-checkbox [checked]="isSelected(role.code)" (change)="toggle(role.code, $event.checked)">
            {{ role.name }} <span class="muted">({{ role.code }})</span>
          </mat-checkbox>
        }
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="close()">Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="current().length === 0" (click)="save()">Guardar</button>
    </mat-dialog-actions>
  `,
})
export class AssignRolesDialogComponent {
  protected readonly data = inject<AssignRolesData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<AssignRolesDialogComponent, string[] | null>);

  protected readonly current = signal<string[]>([...this.data.selected]);

  protected isSelected(code: string): boolean {
    return this.current().includes(code);
  }

  protected toggle(code: string, checked: boolean): void {
    this.current.update((list) =>
      checked ? [...new Set([...list, code])] : list.filter((c) => c !== code),
    );
  }

  protected save(): void {
    this.ref.close(this.current());
  }

  protected close(): void {
    this.ref.close(null);
  }
}
