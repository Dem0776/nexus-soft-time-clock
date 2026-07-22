package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceResult;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.IdempotencyStorePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IdempotencyStoreAdapter implements IdempotencyStorePort {

    private final IdempotencyKeyJpaRepository jpa;
    private final ObjectMapper objectMapper;

    public IdempotencyStoreAdapter(IdempotencyKeyJpaRepository jpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<AttendanceResult> find(UUID tenantId, UUID operationUuid) {
        return jpa.findByTenantIdAndOperationUuid(tenantId, operationUuid)
                .map(e -> deserialize(e.getResultJson()));
    }

    @Override
    public void save(UUID tenantId, UUID operationUuid, AttendanceResult result) {
        Instant expiresAt = Instant.now().plus(60, ChronoUnit.DAYS);
        jpa.save(new IdempotencyKeyJpaEntity(UUID.randomUUID(), tenantId, operationUuid,
                "POST /api/v1/attendance", 200, result.recordId(), result.serverTime(),
                serialize(result), expiresAt));
    }

    private String serialize(AttendanceResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar el resultado de asistencia", e);
        }
    }

    private AttendanceResult deserialize(String json) {
        try {
            return objectMapper.readValue(json, AttendanceResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer el resultado idempotente", e);
        }
    }
}
