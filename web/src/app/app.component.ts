import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Punto de montaje raíz. El shell (toolbar + navegación) vive en MainLayoutComponent,
 * que envuelve las páginas autenticadas; el login se renderiza sin shell.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`,
})
export class AppComponent {}
