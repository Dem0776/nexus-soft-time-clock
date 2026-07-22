package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "operation_uuid", nullable = false)
    private UUID operationUuid;

    @Column(nullable = false)
    private String endpoint;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "attendance_id")
    private UUID attendanceId;

    @Column(name = "attendance_server_time")
    private Instant attendanceServerTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_json")
    private String resultJson;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyKeyJpaEntity() {
    }

    public IdempotencyKeyJpaEntity(UUID id, UUID tenantId, UUID operationUuid, String endpoint,
                                   Integer responseStatus, UUID attendanceId, Instant attendanceServerTime,
                                   String resultJson, Instant expiresAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.operationUuid = operationUuid;
        this.endpoint = endpoint;
        this.responseStatus = responseStatus;
        this.attendanceId = attendanceId;
        this.attendanceServerTime = attendanceServerTime;
        this.resultJson = resultJson;
        this.expiresAt = expiresAt;
    }

    public String getResultJson() {
        return resultJson;
    }
}
