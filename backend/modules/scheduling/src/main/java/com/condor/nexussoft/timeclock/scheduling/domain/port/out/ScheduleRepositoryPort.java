package com.condor.nexussoft.timeclock.scheduling.domain.port.out;

import com.condor.nexussoft.timeclock.scheduling.domain.Schedule;
import com.condor.nexussoft.timeclock.shared.domain.Paged;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepositoryPort {

    Schedule save(Schedule schedule);

    Schedule update(Schedule schedule);

    Optional<Schedule> findByIdAndTenant(UUID id, UUID tenantId);

    boolean existsByTenantAndCode(UUID tenantId, String code);

    Paged<Schedule> findAllByTenant(UUID tenantId, int page, int size, String search);
}
