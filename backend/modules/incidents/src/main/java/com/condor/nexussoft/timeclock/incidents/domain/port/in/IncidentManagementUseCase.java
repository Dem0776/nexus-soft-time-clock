package com.condor.nexussoft.timeclock.incidents.domain.port.in;

import com.condor.nexussoft.timeclock.incidents.domain.Incident;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

/** Gestión de incidencias (RF-09). */
public interface IncidentManagementUseCase {

    Paged<Incident> list(UUID tenantId, String status, int page, int size);

    Incident resolve(UUID tenantId, UUID incidentId, String status, String note, UUID resolverId);

    /** Alta automática de incidencia por registro rechazado (consumida de un evento). */
    Incident openForRejectedAttendance(UUID tenantId, UUID userId, UUID attendanceId, String reason);
}
