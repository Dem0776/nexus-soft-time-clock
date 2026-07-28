package com.condor.nexussoft.timeclock.hr.domain.port.out;

import com.condor.nexussoft.timeclock.hr.domain.EmployeeProfile;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeProfileRepositoryPort {
    Optional<EmployeeProfile> findByUserId(UUID tenantId, UUID userId);
    EmployeeProfile save(EmployeeProfile profile);
}
