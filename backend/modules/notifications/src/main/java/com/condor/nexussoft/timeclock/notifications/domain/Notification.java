package com.condor.nexussoft.timeclock.notifications.domain;

import java.util.UUID;

/** Notificación a un usuario. Canal PUSH/EMAIL/INAPP; ciclo PENDING→SENT/FAILED/READ. */
public class Notification {

    public enum Channel { PUSH, EMAIL, INAPP }

    public enum Status { PENDING, SENT, FAILED, READ }

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final Channel channel;
    private final String type;
    private final String title;
    private final String body;
    private Status status;

    public Notification(UUID id, UUID tenantId, UUID userId, Channel channel, String type,
                        String title, String body, Status status) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.channel = channel;
        this.type = type;
        this.title = title;
        this.body = body;
        this.status = status;
    }

    public static Notification forRejectedAttendance(UUID tenantId, UUID userId, String reason) {
        return new Notification(UUID.randomUUID(), tenantId, userId, Channel.PUSH, "ATTENDANCE_REJECTED",
                "Registro rechazado", "Tu registro de asistencia fue rechazado: " + reason, Status.PENDING);
    }

    public void markSent() {
        this.status = Status.SENT;
    }

    public void markFailed() {
        this.status = Status.FAILED;
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID userId() { return userId; }
    public Channel channel() { return channel; }
    public String type() { return type; }
    public String title() { return title; }
    public String body() { return body; }
    public Status status() { return status; }
}
