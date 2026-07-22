package com.condor.nexussoft.timeclock.audit.domain.port.out;

import com.condor.nexussoft.timeclock.audit.domain.AuditLogEntry;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.UUID;

public interface AuditLogRepositoryPort {

    /** Append-only: solo inserción (RN-61). */
    void append(AuditLogEntry entry);

    Paged<AuditLogEntry> findByTenant(UUID tenantId, int page, int size);
}
