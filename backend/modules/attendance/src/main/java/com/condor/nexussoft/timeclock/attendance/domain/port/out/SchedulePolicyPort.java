package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.time.Instant;
import java.util.UUID;

/** Evalúa si el registro cae dentro de la ventana del turno asignado al colaborador (RN-15). */
public interface SchedulePolicyPort {

    ScheduleDecision check(UUID tenantId, UUID userId, UUID workSiteId, Instant at);

    enum Outcome {
        /** El colaborador no tiene turno asignado vigente en ese centro → sin restricción horaria. */
        NO_SCHEDULE,
        /** Hay turno asignado y el registro cae dentro de su ventana. */
        WITHIN_WINDOW,
        /** Hay turno asignado pero el registro cae fuera de la ventana → OUT_OF_SCHEDULE. */
        OUT_OF_WINDOW
    }

    /**
     * Resultado de la evaluación horaria. {@code minutesLate} es la tardanza sobre
     * {@code inicio_turno + tolerancia} de la ocurrencia que casó la ventana (RN-16); es 0 salvo
     * que la marca sea posterior a la tolerancia. Solo tiene sentido para ENTRADA dentro de ventana.
     */
    record ScheduleDecision(Outcome outcome, int minutesLate) {

        public static ScheduleDecision noSchedule() {
            return new ScheduleDecision(Outcome.NO_SCHEDULE, 0);
        }

        public static ScheduleDecision outOfWindow() {
            return new ScheduleDecision(Outcome.OUT_OF_WINDOW, 0);
        }

        public static ScheduleDecision withinWindow(int minutesLate) {
            return new ScheduleDecision(Outcome.WITHIN_WINDOW, Math.max(0, minutesLate));
        }
    }
}
