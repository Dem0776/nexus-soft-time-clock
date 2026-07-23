package com.condor.nexussoft.timeclock.attendance.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleWindowValidatorTest {

    private final LocalTime start = LocalTime.of(9, 0);
    private final LocalTime end = LocalTime.of(18, 0);

    private boolean within(LocalDateTime now) {
        // ventana: 08:45 (9:00 - 15) .. 18:30 (18:00 + 30)
        return ScheduleWindowValidator.withinWindow(start, end, false, 15, 30, now);
    }

    @Test
    void dentroDeLaVentanaDeEntrada() {
        assertThat(within(LocalDateTime.of(2026, 7, 21, 8, 50))).isTrue();  // 10 min antes, dentro de tolerancia
    }

    @Test
    void justoAntesDeLaVentana_esFuera() {
        assertThat(within(LocalDateTime.of(2026, 7, 21, 8, 30))).isFalse(); // 30 min antes, fuera
    }

    @Test
    void dentroDeLaJornada() {
        assertThat(within(LocalDateTime.of(2026, 7, 21, 13, 0))).isTrue();
    }

    @Test
    void despuesDeLaVentanaDeSalida_esFuera() {
        assertThat(within(LocalDateTime.of(2026, 7, 21, 19, 0))).isFalse(); // pasada la tolerancia de salida
    }

    @Test
    void turnoNocturno_cruzaMedianoche_madrugadaSiguienteEstaDentro() {
        // turno 22:00 -> 06:00, ventana 21:45 .. 06:30 del día siguiente
        LocalTime nightStart = LocalTime.of(22, 0);
        LocalTime nightEnd = LocalTime.of(6, 0);
        LocalDateTime madrugada = LocalDateTime.of(2026, 7, 22, 2, 0);  // 02:00 del día siguiente
        assertThat(ScheduleWindowValidator.withinWindow(nightStart, nightEnd, true, 15, 30, madrugada)).isTrue();
    }

    @Test
    void turnoNocturno_fueraDeVentana_alMediodia() {
        LocalTime nightStart = LocalTime.of(22, 0);
        LocalTime nightEnd = LocalTime.of(6, 0);
        LocalDateTime mediodia = LocalDateTime.of(2026, 7, 22, 12, 0);
        assertThat(ScheduleWindowValidator.withinWindow(nightStart, nightEnd, true, 15, 30, mediodia)).isFalse();
    }
}
