package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.time.Instant;
import java.util.UUID;

/** Evalúa si el registro cae dentro de la ventana del turno asignado al colaborador (RN-15). */
public interface SchedulePolicyPort {

    ScheduleCheck check(UUID tenantId, UUID userId, UUID workSiteId, Instant at);

    enum ScheduleCheck {
        /** El colaborador no tiene turno asignado vigente en ese centro → sin restricción horaria. */
        NO_SCHEDULE,
        /** Hay turno asignado y el registro cae dentro de su ventana. */
        WITHIN_WINDOW,
        /** Hay turno asignado pero el registro cae fuera de la ventana → OUT_OF_SCHEDULE. */
        OUT_OF_WINDOW
    }
}
