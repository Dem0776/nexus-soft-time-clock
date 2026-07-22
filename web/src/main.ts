// sockjs-client (usado por el cliente STOMP del mapa en tiempo real) referencia `global`,
// ausente en el navegador; se apunta a globalThis antes de arrancar la app.
(globalThis as unknown as { global: typeof globalThis }).global ??= globalThis;

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
