package com.condor.nexussoft.timeclock.attendance.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Determina si un instante (ya convertido a la zona del turno) cae dentro de la <b>ventana de
 * registro</b> de un turno (RN-15): {@code [inicio - windowBefore, fin + windowAfter]}.
 * Para turnos que cruzan medianoche se evalúa tanto la ocurrencia que empieza hoy como la que
 * empezó ayer (un turno nocturno abierto anoche sigue vigente en la madrugada).
 */
public final class ScheduleWindowValidator {

    private ScheduleWindowValidator() {
    }

    public static boolean withinWindow(LocalTime start, LocalTime end, boolean crossesMidnight,
                                       int windowBeforeMin, int windowAfterMin, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (inWindowForStartDate(today, start, end, crossesMidnight, windowBeforeMin, windowAfterMin, now)) {
            return true;
        }
        return crossesMidnight && inWindowForStartDate(today.minusDays(1), start, end, true,
                windowBeforeMin, windowAfterMin, now);
    }

    private static boolean inWindowForStartDate(LocalDate startDate, LocalTime start, LocalTime end,
                                                boolean crossesMidnight, int beforeMin, int afterMin,
                                                LocalDateTime now) {
        LocalDateTime windowStart = startDate.atTime(start).minusMinutes(beforeMin);
        LocalDate endDate = crossesMidnight ? startDate.plusDays(1) : startDate;
        LocalDateTime windowEnd = endDate.atTime(end).plusMinutes(afterMin);
        return !now.isBefore(windowStart) && !now.isAfter(windowEnd);
    }
}
