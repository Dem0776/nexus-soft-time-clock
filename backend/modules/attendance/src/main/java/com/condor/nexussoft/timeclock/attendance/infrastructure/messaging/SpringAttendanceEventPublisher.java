package com.condor.nexussoft.timeclock.attendance.infrastructure.messaging;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.AttendanceEventPublisherPort;
import com.condor.nexussoft.timeclock.platform.outbox.OutboxWriter;
import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;
import org.springframework.stereotype.Component;

/**
 * Escribe los eventos de asistencia en el Transactional Outbox (ADR-005). El relay los
 * publica al bus tras el commit, desacoplando auditoría, incidencias, notificaciones y tiempo real.
 */
@Component
public class SpringAttendanceEventPublisher implements AttendanceEventPublisherPort {

    private final OutboxWriter outboxWriter;

    public SpringAttendanceEventPublisher(OutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    @Override
    public void publish(DomainEvent event) {
        outboxWriter.write(event);
    }
}
