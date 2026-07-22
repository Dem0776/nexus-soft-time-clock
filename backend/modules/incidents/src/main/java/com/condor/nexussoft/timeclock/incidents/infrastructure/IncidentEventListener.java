package com.condor.nexussoft.timeclock.incidents.infrastructure;

import com.condor.nexussoft.timeclock.attendance.domain.event.AttendanceRejected;
import com.condor.nexussoft.timeclock.incidents.domain.port.in.IncidentManagementUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Regla de negocio automatizada (event-driven): cada rechazo de asistencia abre una
 * incidencia REGISTRO_RECHAZADO para su revisión por RR.HH./supervisor (RF-09).
 */
@Component
public class IncidentEventListener {

    private final IncidentManagementUseCase incidents;

    public IncidentEventListener(IncidentManagementUseCase incidents) {
        this.incidents = incidents;
    }

    @EventListener
    public void onAttendanceRejected(AttendanceRejected event) {
        incidents.openForRejectedAttendance(event.tenantId(), event.userId(),
                event.attendanceId(), event.reason());
    }
}
