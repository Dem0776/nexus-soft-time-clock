package com.condor.nexussoft.timeclock.platform.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Fila del outbox transaccional (ADR-005). */
@Entity
@Table(name = "outbox_events")
public class OutboxEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_class")
    private String eventClass;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private String status;

    protected OutboxEventJpaEntity() {
    }

    public OutboxEventJpaEntity(UUID id, UUID tenantId, String aggregateType, String aggregateId,
                                String eventType, String eventClass, String payload, Instant occurredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventClass = eventClass;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.attempts = 0;
        this.status = "PENDING";
    }

    public UUID getId() { return id; }
    public String getEventClass() { return eventClass; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }

    public void markPublished(Instant when) {
        this.status = "PUBLISHED";
        this.publishedAt = when;
    }

    public void markFailed() {
        this.attempts++;
        if (this.attempts >= 10) {
            this.status = "FAILED";
        }
    }
}
