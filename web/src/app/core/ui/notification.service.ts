import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** Avisos efímeros (éxito/error) mediante MatSnackBar, reutilizable en todas las features. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snack = inject(MatSnackBar);

  success(message: string): void {
    this.snack.open(message, 'OK', { duration: 3000 });
  }

  error(message: string): void {
    this.snack.open(message, 'Cerrar', { duration: 6000 });
  }
}
