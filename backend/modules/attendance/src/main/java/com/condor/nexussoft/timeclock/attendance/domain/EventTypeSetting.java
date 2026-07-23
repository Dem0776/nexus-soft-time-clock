package com.condor.nexussoft.timeclock.attendance.domain;

/** Configuración por empresa de un tipo de evento intermedio (HU-12 CA1): habilitado y etiqueta. */
public record EventTypeSetting(AttendanceEventType eventType, boolean enabled, String label) {
}
