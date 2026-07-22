package com.condor.nexussoft.timeclock.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raíz de agregado (DDD). Acumula eventos de dominio que la capa de aplicación
 * publicará tras persistir el agregado (patrón outbox, ADR-005).
 *
 * @param <ID> tipo del identificador del agregado
 */
public abstract class AggregateRoot<ID> {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    public abstract ID getId();

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
