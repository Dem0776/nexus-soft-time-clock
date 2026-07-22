package com.condor.nexussoft.timeclock.identity.domain.port.out;

import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;

import java.util.Collection;

/**
 * Publica eventos de dominio. En esta iteración el adaptador usa el bus in-process
 * de Spring; en iteraciones posteriores se respalda con el Transactional Outbox (ADR-005).
 */
public interface DomainEventPublisherPort {

    void publishAll(Collection<DomainEvent> events);
}
