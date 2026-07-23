package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceEventType;
import com.condor.nexussoft.timeclock.attendance.domain.EventTypeSetting;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persistencia de la configuración de tipos de evento por empresa (HU-12 CA1). */
public interface EventTypeConfigPort {

    Map<AttendanceEventType, EventTypeSetting> findByTenant(UUID tenantId);

    /** Reemplaza la configuración de tipos intermedios del tenant por la lista dada. */
    void replaceAll(UUID tenantId, List<EventTypeSetting> settings);
}
