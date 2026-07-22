import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

/** Tarjeta KPI: ícono con color de acento, valor grande y etiqueta. */
@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="stat-card">
      <div class="stat-icon {{ tone() }}"><mat-icon>{{ icon() }}</mat-icon></div>
      <div class="stat-body">
        <div class="stat-value">{{ value() }}</div>
        <div class="stat-label">{{ label() }}</div>
      </div>
    </div>
  `,
  styles: [
    `
      .stat-card {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 18px;
        background: var(--surface);
        border: 1px solid var(--border);
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-1);
      }
      .stat-icon {
        width: 46px;
        height: 46px;
        border-radius: 12px;
        display: grid;
        place-items: center;
        flex: 0 0 auto;
      }
      .stat-icon mat-icon { color: inherit; }
      .stat-icon.success { color: var(--success); background: var(--success-bg); }
      .stat-icon.warning { color: var(--warning); background: var(--warning-bg); }
      .stat-icon.danger { color: var(--danger); background: var(--danger-bg); }
      .stat-icon.info { color: var(--info); background: var(--info-bg); }
      .stat-icon.neutral { color: var(--neutral); background: var(--neutral-bg); }
      .stat-value { font-size: 1.9rem; font-weight: 700; line-height: 1.1; }
      .stat-label { color: var(--text-muted); font-size: 0.85rem; margin-top: 2px; }
    `,
  ],
})
export class StatCardComponent {
  readonly icon = input.required<string>();
  readonly value = input.required<number | string>();
  readonly label = input.required<string>();
  readonly tone = input<Tone>('info');
}
