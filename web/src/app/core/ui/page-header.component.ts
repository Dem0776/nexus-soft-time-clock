import { Component, Input } from '@angular/core';

/** Encabezado de página: título (y subtítulo opcional) a la izquierda, acciones proyectadas a la derecha. */
@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <div class="page-header">
      <div class="titles">
        <h2>{{ title }}</h2>
        @if (subtitle) { <p class="subtitle">{{ subtitle }}</p> }
      </div>
      <span class="spacer"></span>
      <div class="actions"><ng-content></ng-content></div>
    </div>
  `,
  styles: [
    `
      .page-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin: 0 0 20px;
        flex-wrap: wrap;
      }
      .titles h2 {
        margin: 0;
        font-size: var(--font-page-title);
        font-weight: 700;
        letter-spacing: -0.01em;
      }
      .subtitle {
        margin: 4px 0 0;
        color: var(--text-muted);
        font-size: var(--font-body);
      }
      .spacer { flex: 1 1 auto; }
      .actions { display: flex; gap: 8px; align-items: center; }
    `,
  ],
})
export class PageHeaderComponent {
  @Input({ required: true }) title = '';
  @Input() subtitle?: string;
}
