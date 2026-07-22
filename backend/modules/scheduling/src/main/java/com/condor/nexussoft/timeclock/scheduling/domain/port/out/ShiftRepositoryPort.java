package com.condor.nexussoft.timeclock.scheduling.domain.port.out;

import com.condor.nexussoft.timeclock.scheduling.domain.Shift;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftRepositoryPort {

    Shift save(Shift shift);

    Shift update(Shift shift);

    Optional<Shift> findByIdAndTenant(UUID id, UUID tenantId);

    List<Shift> findByScheduleAndTenant(UUID scheduleId, UUID tenantId);
}
