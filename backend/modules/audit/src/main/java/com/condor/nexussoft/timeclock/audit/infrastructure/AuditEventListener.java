package com.condor.nexussoft.timeclock.audit.infrastructure;

import com.condor.nexussoft.timeclock.audit.application.AuditRecorder;
import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Escucha TODOS los eventos de dominio (cualquier implementación de {@link DomainEvent})
 * y los registra en la bitácora. Al escuchar la interfaz, no se acopla a ningún BC emisor.
 */
@Component
public class AuditEventListener {

    private final AuditRecorder recorder;

    public AuditEventListener(AuditRecorder recorder) {
        this.recorder = recorder;
    }

    @EventListener
    public void onDomainEvent(DomainEvent event) {
        recorder.record(event);
    }
}
