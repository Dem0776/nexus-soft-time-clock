package com.condor.nexussoft.timeclock.notifications.domain.port.out;

import com.condor.nexussoft.timeclock.notifications.domain.Notification;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Paged<Notification> findByUser(UUID tenantId, UUID userId, int page, int size);
}
