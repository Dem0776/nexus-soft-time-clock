package com.condor.nexussoft.timeclock.identity.infrastructure.messaging;

import com.condor.nexussoft.timeclock.identity.domain.port.out.DomainEventPublisherPort;
import com.condor.nexussoft.timeclock.platform.outbox.OutboxWriter;
import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Escribe los eventos de dominio en el Transactional Outbox (ADR-005), dentro de la misma
 * transacción de negocio. El relay los publica al bus tras el commit — entrega fiable.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisherPort {

    private final OutboxWriter outboxWriter;

    public SpringDomainEventPublisher(OutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    @Override
    public void publishAll(Collection<DomainEvent> events) {
        events.forEach(outboxWriter::write);
    }
}
