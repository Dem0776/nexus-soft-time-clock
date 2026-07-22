package com.condor.nexussoft.timeclock.notifications.infrastructure;

import com.condor.nexussoft.timeclock.attendance.domain.event.AttendanceRejected;
import com.condor.nexussoft.timeclock.notifications.domain.port.in.NotificationUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Notifica al colaborador cuando su registro es rechazado (event-driven, RF-27). */
@Component
public class NotificationEventListener {

    private final NotificationUseCase notifications;

    public NotificationEventListener(NotificationUseCase notifications) {
        this.notifications = notifications;
    }

    @EventListener
    public void onAttendanceRejected(AttendanceRejected event) {
        notifications.notifyRejectedAttendance(event.tenantId(), event.userId(), event.reason());
    }
}
