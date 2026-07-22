package com.condor.nexussoft.timeclock.notifications.domain.port.out;

import java.util.UUID;

/** Envío de notificación push (adaptador FCM en producción). */
public interface PushSenderPort {

    boolean send(UUID userId, String title, String body);
}
