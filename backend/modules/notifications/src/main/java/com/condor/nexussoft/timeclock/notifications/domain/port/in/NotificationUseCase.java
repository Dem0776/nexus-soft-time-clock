package com.condor.nexussoft.timeclock.notifications.domain.port.in;

import com.condor.nexussoft.timeclock.notifications.domain.Notification;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

/** Notificaciones (RF-27). */
public interface NotificationUseCase {

    /** Notifica a un usuario que su registro fue rechazado (consumido de un evento). */
    void notifyRejectedAttendance(UUID tenantId, UUID userId, String reason);

    Paged<Notification> list(UUID tenantId, UUID userId, int page, int size);
}
