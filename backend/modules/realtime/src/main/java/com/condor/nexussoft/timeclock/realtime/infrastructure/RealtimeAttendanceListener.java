package com.condor.nexussoft.timeclock.realtime.infrastructure;

import com.condor.nexussoft.timeclock.attendance.domain.event.AttendanceRegistered;
import com.condor.nexussoft.timeclock.attendance.domain.event.AttendanceRejected;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Proyecta los eventos de asistencia a los suscriptores del tenant (mapa/dashboard en vivo,
 * RF-25). Consume del bus in-process (event-driven) y publica por WebSocket.
 */
@Component
public class RealtimeAttendanceListener {

    private final SimpMessagingTemplate messaging;

    public RealtimeAttendanceListener(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @org.springframework.context.event.EventListener
    public void onRegistered(AttendanceRegistered e) {
        send(e.tenantId(), Map.of(
                "type", "ACCEPTED",
                "attendanceId", e.attendanceId().toString(),
                "userId", e.userId().toString(),
                "workSiteId", e.workSiteId().toString(),
                "eventKind", e.eventKind(),
                "occurredAt", e.occurredAt().toString()));
    }

    @org.springframework.context.event.EventListener
    public void onRejected(AttendanceRejected e) {
        send(e.tenantId(), Map.of(
                "type", "REJECTED",
                "attendanceId", e.attendanceId().toString(),
                "userId", e.userId().toString(),
                "reason", e.reason(),
                "occurredAt", e.occurredAt().toString()));
    }

    private void send(UUID tenantId, Map<String, Object> payload) {
        messaging.convertAndSend("/topic/tenant/" + tenantId + "/attendance", payload);
    }
}
