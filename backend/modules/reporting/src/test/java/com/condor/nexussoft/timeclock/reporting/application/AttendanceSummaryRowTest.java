package com.condor.nexussoft.timeclock.reporting.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceSummaryRowTest {

    @Test
    void of_derivaHorasTotalesYCumplimiento() {
        AttendanceSummaryRow row = AttendanceSummaryRow.of(
                "E-001", "Ana Pérez", "Sucursal Centro",
                20, 19, 1, 0, 2,
                9000, 300, true); // 150 h trabajadas + 5 h extra

        assertThat(row.workedHours()).isEqualTo(150.0);
        assertThat(row.overtimeHours()).isEqualTo(5.0);
        assertThat(row.totalHours()).isEqualTo(155.0);
        assertThat(row.compliancePercentage()).isEqualTo(95.0); // 19/20
        assertThat(row.justifiedAbsences()).isEqualTo(1);
        assertThat(row.unjustifiedAbsences()).isZero();
        assertThat(row.active()).isTrue();
    }

    @Test
    void of_cumplimientoCeroCuandoNoHayDiasEsperados() {
        AttendanceSummaryRow row = AttendanceSummaryRow.of(
                "E-002", "Luis Gómez", "—",
                0, 0, 0, 3, 0,
                0, 0, false);

        assertThat(row.compliancePercentage()).isZero();
        assertThat(row.unjustifiedAbsences()).isEqualTo(3);
        assertThat(row.active()).isFalse();
    }

    @Test
    void of_redondeaCumplimientoAUnDecimal() {
        AttendanceSummaryRow row = AttendanceSummaryRow.of(
                "E-003", "María Ruiz", "Planta Norte",
                22, 15, 0, 0, 0,
                0, 0, true);

        // 15/22 = 68.18... -> 68.2
        assertThat(row.compliancePercentage()).isEqualTo(68.2);
    }
}
