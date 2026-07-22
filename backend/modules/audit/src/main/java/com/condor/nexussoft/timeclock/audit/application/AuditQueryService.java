package com.condor.nexussoft.timeclock.audit.application;

import com.condor.nexussoft.timeclock.audit.domain.AuditLogEntry;
import com.condor.nexussoft.timeclock.audit.domain.port.in.AuditQueryUseCase;
import com.condor.nexussoft.timeclock.audit.domain.port.out.AuditLogRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditQueryService implements AuditQueryUseCase {

    private final AuditLogRepositoryPort auditLog;

    public AuditQueryService(AuditLogRepositoryPort auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<AuditLogEntry> list(UUID tenantId, int page, int size) {
        return auditLog.findByTenant(tenantId, page, size);
    }
}
