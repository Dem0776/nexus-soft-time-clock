package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceResult;

import java.util.Optional;
import java.util.UUID;

/** Idempotencia por UUID de operación (ADR-004): evita duplicar reenvíos y detecta replay. */
public interface IdempotencyStorePort {

    Optional<AttendanceResult> find(UUID tenantId, UUID operationUuid);

    void save(UUID tenantId, UUID operationUuid, AttendanceResult result);
}
