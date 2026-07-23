package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import com.condor.nexussoft.timeclock.attendance.domain.EventTypeSetting;

import java.util.List;
import java.util.UUID;

/** Administración del catálogo de tipos de evento por empresa (HU-12 CA1). */
public interface EventTypeCatalogUseCase {

    /** Los 5 tipos con su estado efectivo (habilitado + etiqueta) para el tenant. */
    List<EventTypeSetting> list(UUID tenantId);

    /** Actualiza la configuración de los tipos intermedios y devuelve el catálogo resultante. */
    List<EventTypeSetting> update(UUID tenantId, List<EventTypeSetting> settings);
}
