package com.condor.nexussoft.timeclock.attendance.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

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
        return matchedStart(start, end, crossesMidnight, windowBeforeMin, windowAfterMin, now).isPresent();
    }

    /**
     * Devuelve el {@code inicio} (fecha+hora) de la ocurrencia del turno cuya ventana contiene a
     * {@code now}, o vacío si {@code now} no cae en ninguna ventana. Sirve para medir la tardanza
     * respecto al inicio real de la jornada (RN-16), respetando turnos que cruzan medianoche.
     */
    public static Optional<LocalDateTime> matchedStart(LocalTime start, LocalTime end, boolean crossesMidnight,
                                                       int windowBeforeMin, int windowAfterMin, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (inWindowForStartDate(today, start, end, crossesMidnight, windowBeforeMin, windowAfterMin, now)) {
            return Optional.of(today.atTime(start));
        }
        if (crossesMidnight && inWindowForStartDate(today.minusDays(1), start, end, true,
                windowBeforeMin, windowAfterMin, now)) {
            return Optional.of(today.minusDays(1).atTime(start));
        }
        return Optional.empty();
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
