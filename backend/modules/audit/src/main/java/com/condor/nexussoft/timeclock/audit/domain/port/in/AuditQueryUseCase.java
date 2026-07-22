package com.condor.nexussoft.timeclock.audit.domain.port.in;

import com.condor.nexussoft.timeclock.audit.domain.AuditLogEntry;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

/** Consulta de la bitácora (RF-12, solo lectura). */
public interface AuditQueryUseCase {

    Paged<AuditLogEntry> list(UUID tenantId, int page, int size);
}
