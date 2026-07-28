package com.condor.nexussoft.timeclock.hr.domain.port.in;

import com.condor.nexussoft.timeclock.hr.domain.BeyondMode;
import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import com.condor.nexussoft.timeclock.hr.domain.VacationTier;
import java.util.List;
import java.util.UUID;

public interface VacationPolicyUseCase {
    VacationPolicy get(UUID tenantId);
    VacationPolicy update(UpdatePolicyCommand command);

    record UpdatePolicyCommand(
            UUID tenantId,
            List<VacationTier> tiers,
            BeyondMode beyondMode,
            int beyondIncrementDays,
            int beyondEveryYears,
            boolean requireApproval,
            boolean countBusinessDaysOnly
    ) {}
}
