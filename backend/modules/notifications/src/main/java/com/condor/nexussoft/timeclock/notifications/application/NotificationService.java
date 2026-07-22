package com.condor.nexussoft.timeclock.notifications.application;

import com.condor.nexussoft.timeclock.notifications.domain.Notification;
import com.condor.nexussoft.timeclock.notifications.domain.port.in.NotificationUseCase;
import com.condor.nexussoft.timeclock.notifications.domain.port.out.NotificationRepositoryPort;
import com.condor.nexussoft.timeclock.notifications.domain.port.out.PushSenderPort;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort notifications;
    private final PushSenderPort push;

    public NotificationService(NotificationRepositoryPort notifications, PushSenderPort push) {
        this.notifications = notifications;
        this.push = push;
    }

    @Override
    @Transactional
    public void notifyRejectedAttendance(UUID tenantId, UUID userId, String reason) {
        Notification notification = Notification.forRejectedAttendance(tenantId, userId, reason);
        boolean sent = push.send(userId, notification.title(), notification.body());
        if (sent) {
            notification.markSent();
        } else {
            notification.markFailed();
        }
        notifications.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<Notification> list(UUID tenantId, UUID userId, int page, int size) {
        return notifications.findByUser(tenantId, userId, page, size);
    }
}
