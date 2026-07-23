package com.condor.nexussoft.timeclock.attendance.domain;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catálogo de tipos de evento con la política por empresa (HU-12 CA1). ENTRADA y SALIDA son el
 * núcleo de la jornada: siempre habilitadas y no configurables. Los tipos intermedios
 * (descansos, cambio de sitio) pueden habilitarse/deshabilitarse y reetiquetarse por tenant.
 * Sin configuración explícita, todo tipo intermedio está habilitado (comportamiento por defecto).
 */
public final class EventTypeCatalog {

    private EventTypeCatalog() {
    }

    public static final Set<AttendanceEventType> CORE =
            EnumSet.of(AttendanceEventType.ENTRADA, AttendanceEventType.SALIDA);

    public static boolean isConfigurable(AttendanceEventType type) {
        return !CORE.contains(type);
    }

    public static boolean isEnabled(AttendanceEventType type, Map<AttendanceEventType, EventTypeSetting> stored) {
        if (CORE.contains(type)) {
            return true;
        }
        EventTypeSetting s = stored.get(type);
        return s == null || s.enabled();   // habilitado por defecto si no hay override
    }

    /** Devuelve los 5 tipos en orden, aplicando overrides de habilitación/etiqueta del tenant. */
    public static List<EventTypeSetting> merge(Map<AttendanceEventType, EventTypeSetting> stored) {
        List<EventTypeSetting> out = new ArrayList<>();
        for (AttendanceEventType t : AttendanceEventType.values()) {
            EventTypeSetting s = stored.get(t);
            boolean enabled = CORE.contains(t) || s == null || s.enabled();
            String label = s != null && s.label() != null && !s.label().isBlank() ? s.label() : defaultLabel(t);
            out.add(new EventTypeSetting(t, enabled, label));
        }
        return out;
    }

    public static String defaultLabel(AttendanceEventType t) {
        return switch (t) {
            case ENTRADA -> "Entrada";
            case SALIDA -> "Salida";
            case INICIO_DESCANSO -> "Inicio de descanso";
            case FIN_DESCANSO -> "Fin de descanso";
            case CAMBIO_SITIO -> "Cambio de sitio";
        };
    }
}
