package com.condor.nexussoft.timeclock.notifications.infrastructure.push;

import com.condor.nexussoft.timeclock.notifications.domain.port.out.PushSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador de push de desarrollo (registra en log). En producción se sustituye por un
 * adaptador Firebase Cloud Messaging (firebase-admin) que resuelve el push_token del dispositivo.
 */
@Component
public class LoggingPushSender implements PushSenderPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushSender.class);

    @Override
    public boolean send(UUID userId, String title, String body) {
        log.info("[PUSH] user={} title='{}' body='{}'", userId, title, body);
        return true;
    }
}
