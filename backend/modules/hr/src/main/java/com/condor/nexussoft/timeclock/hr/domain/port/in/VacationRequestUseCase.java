package com.condor.nexussoft.timeclock.hr.domain.port.in;

import com.condor.nexussoft.timeclock.hr.domain.PagedResult;
import com.condor.nexussoft.timeclock.hr.domain.VacationRequest;
import com.condor.nexussoft.timeclock.hr.domain.VacationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VacationRequestUseCase {
    PagedResult<VacationRequest> list(UUID tenantId, VacationStatus status, String search, int page, int size);

    /** Solicitudes de un colaborador (para su vista "mis vacaciones"). */
    List<VacationRequest> listForUser(UUID tenantId, UUID userId);

    VacationRequest create(CreateCommand command);

    VacationRequest resolve(ResolveCommand command);

    record CreateCommand(UUID tenantId, UUID userId, LocalDate startDate, LocalDate endDate, String reason) {}

    /** approve=true → APPROVED; approve=false → REJECTED. */
    record ResolveCommand(UUID tenantId, UUID requestId, UUID resolvedBy, boolean approve, String note) {}
}
