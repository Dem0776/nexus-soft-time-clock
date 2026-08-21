package com.condor.nexussoft.timeclock.tenancy.domain.port.out;

import com.condor.nexussoft.timeclock.tenancy.domain.CompanySettings;

import java.util.Optional;
import java.util.UUID;

public interface CompanySettingsRepositoryPort {

    Optional<CompanySettings> find(UUID tenantId);

    CompanySettings save(CompanySettings settings);
}
