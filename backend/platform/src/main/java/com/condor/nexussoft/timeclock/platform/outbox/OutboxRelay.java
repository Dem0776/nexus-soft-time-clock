package com.condor.nexussoft.timeclock.platform.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Poller del outbox: publica los eventos pendientes al bus interno tras el commit de negocio.
 * En despliegue multi-réplica se coordina con un lock (ShedLock) — pendiente en producción.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventJpaRepository repository;
    private final OutboxProcessor processor;

    public OutboxRelay(OutboxEventJpaRepository repository, OutboxProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "${outbox.relay-delay-ms:2000}")
    public void relay() {
        var pending = repository.findTop100ByStatusOrderByOccurredAtAsc("PENDING");
        for (OutboxEventJpaEntity row : pending) {
            try {
                processor.processOne(row.getId());
            } catch (Exception e) {
                log.warn("Fallo al publicar evento outbox {}: {}", row.getId(), e.toString());
                processor.markFailed(row.getId());
            }
        }
    }
}
