package com.condor.nexussoft.timeclock.attendance.infrastructure.integration;

import com.condor.nexussoft.timeclock.attendance.domain.ScheduleWindowValidator;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.SchedulePolicyPort;
import com.condor.nexussoft.timeclock.scheduling.domain.Shift;
import com.condor.nexussoft.timeclock.scheduling.domain.ShiftAssignment;
import com.condor.nexussoft.timeclock.scheduling.domain.port.in.SchedulingUseCase;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Puente hacia Scheduling: busca el turno asignado vigente del colaborador en el centro y evalúa
 * la ventana de registro (RN-15). Sin turno asignado vigente no impone restricción horaria.
 */
@Component
public class SchedulePolicyAdapter implements SchedulePolicyPort {

    private static final ZoneId FALLBACK_ZONE = ZoneId.of("UTC");

    private final SchedulingUseCase scheduling;

    public SchedulePolicyAdapter(SchedulingUseCase scheduling) {
        this.scheduling = scheduling;
    }

    @Override
    public ScheduleCheck check(UUID tenantId, UUID userId, UUID workSiteId, Instant at) {
        List<ShiftAssignment> forSite = scheduling.listAssignments(tenantId, userId).stream()
                .filter(a -> workSiteId.equals(a.workSiteId()))
                .toList();

        boolean anyEffectiveToday = false;
        for (ShiftAssignment a : forSite) {
            Shift shift = safeShift(tenantId, a.shiftId());
            if (shift == null) {
                continue;
            }
            ZoneId zone = zoneForShift(tenantId, shift);
            LocalDateTime nowLocal = LocalDateTime.ofInstant(at, zone);
            if (!isEffectiveOn(a, nowLocal.toLocalDate())) {
                continue;   // asignación no vigente hoy en la zona del turno
            }
            anyEffectiveToday = true;
            if (ScheduleWindowValidator.withinWindow(shift.startTime(), shift.endTime(),
                    shift.crossesMidnight(), shift.windowBeforeMin(), shift.windowAfterMin(), nowLocal)) {
                return ScheduleCheck.WITHIN_WINDOW;
            }
        }
        // Sin turno vigente hoy no se restringe; con turno vigente pero fuera de ventana → rechazo.
        return anyEffectiveToday ? ScheduleCheck.OUT_OF_WINDOW : ScheduleCheck.NO_SCHEDULE;
    }

    private boolean isEffectiveOn(ShiftAssignment a, LocalDate date) {
        boolean startedOk = !date.isBefore(a.validFrom());
        boolean notEnded = a.validTo() == null || !date.isAfter(a.validTo());
        return startedOk && notEnded;
    }

    private ZoneId zoneForShift(UUID tenantId, Shift shift) {
        try {
            String tz = scheduling.getSchedule(tenantId, shift.scheduleId()).timezone();
            return tz == null || tz.isBlank() ? FALLBACK_ZONE : ZoneId.of(tz);
        } catch (RuntimeException e) {
            return FALLBACK_ZONE;
        }
    }

    private Shift safeShift(UUID tenantId, UUID shiftId) {
        try {
            return scheduling.getShift(tenantId, shiftId);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }
}
