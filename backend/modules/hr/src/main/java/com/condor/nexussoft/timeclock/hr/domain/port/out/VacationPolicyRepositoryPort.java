package com.condor.nexussoft.timeclock.hr.domain.port.out;

import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import java.util.Optional;
import java.util.UUID;

public interface VacationPolicyRepositoryPort {
    Optional<VacationPolicy> findByTenant(UUID tenantId);
    VacationPolicy save(VacationPolicy policy);
}
