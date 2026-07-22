import { Component, computed, input } from '@angular/core';

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

const TONE_BY_STATUS: Record<string, Tone> = {
  ACTIVE: 'success',
  APPROVED: 'success',
  ACCEPTED: 'success',
  RESOLVED: 'success',
  READ: 'success',
  OPEN: 'warning',
  SUSPENDED: 'warning',
  LOCKED: 'warning',
  PENDING: 'warning',
  REJECTED: 'danger',
  CLOSED: 'danger',
  FAILED: 'danger',
  INVITED: 'info',
  SENT: 'info',
  INACTIVE: 'neutral',
};

const LABEL_BY_STATUS: Record<string, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
  SUSPENDED: 'Suspendido',
  LOCKED: 'Bloqueado',
  INVITED: 'Invitado',
  OPEN: 'Abierta',
  APPROVED: 'Aprobada',
  REJECTED: 'Rechazada',
  RESOLVED: 'Resuelta',
  CLOSED: 'Cerrada',
  ACCEPTED: 'Aceptado',
  PENDING: 'Pendiente',
  READ: 'Leída',
  SENT: 'Enviada',
};

/** Píldora de color que representa un estado de dominio (RN de presentación). */
@Component({
  selector: 'app-status-chip',
  standalone: true,
  template: `<span class="status-chip {{ tone() }}">{{ label() }}</span>`,
})
export class StatusChipComponent {
  readonly status = input.required<string>();

  protected readonly tone = computed<Tone>(() => TONE_BY_STATUS[this.status()?.toUpperCase()] ?? 'neutral');
  protected readonly label = computed(() => {
    const raw = this.status() ?? '';
    return LABEL_BY_STATUS[raw.toUpperCase()] ?? raw;
  });
}
