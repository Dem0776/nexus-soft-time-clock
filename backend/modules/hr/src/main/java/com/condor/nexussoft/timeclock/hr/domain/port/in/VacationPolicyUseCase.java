package com.condor.nexussoft.timeclock.hr.domain.port.in;

import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import java.util.UUID;

public interface VacationPolicyUseCase {
    VacationPolicy get(UUID tenantId);
    VacationPolicy update(UpdatePolicyCommand command);

    record UpdatePolicyCommand(
            UUID tenantId,
            int daysPerYear,
            boolean requireApproval,
            boolean countBusinessDaysOnly
    ) {}
}
