package com.condor.nexussoft.timeclock.reporting.application;

/**
 * Fila agregada del reporte de asistencia por colaborador (RF-11).
 * Los nombres de campo son camelCase para serializar directamente al contrato del front.
 */
public record AttendanceSummaryRow(
        String employeeNumber,
        String employeeName,
        String workCenter,
        int expectedDays,
        int attendedDays,
        int justifiedAbsences,
        int unjustifiedAbsences,
        int lateArrivals,
        double workedHours,
        double overtimeHours,
        double totalHours,
        boolean active,
        double compliancePercentage) {

    /**
     * Construye la fila a partir de los agregados crudos (minutos), derivando horas totales y
     * el porcentaje de cumplimiento. Centraliza el redondeo para que el cálculo sea verificable.
     */
    public static AttendanceSummaryRow of(
            String employeeNumber,
            String employeeName,
            String workCenter,
            int expectedDays,
            int attendedDays,
            int justifiedAbsences,
            int unjustifiedAbsences,
            int lateArrivals,
            double workedMinutes,
            double overtimeMinutes,
            boolean active) {

        double workedHours = round1(workedMinutes / 60.0);
        double overtimeHours = round1(overtimeMinutes / 60.0);
        double totalHours = round1(workedHours + overtimeHours);
        double compliance = expectedDays > 0 ? round1(attendedDays * 100.0 / expectedDays) : 0.0;

        return new AttendanceSummaryRow(
                employeeNumber, employeeName, workCenter,
                expectedDays, attendedDays, justifiedAbsences, unjustifiedAbsences, lateArrivals,
                workedHours, overtimeHours, totalHours, active, compliance);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
