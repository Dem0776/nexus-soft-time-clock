package com.condor.nexussoft.timeclock.scheduling.domain.port.out;

import com.condor.nexussoft.timeclock.scheduling.domain.ShiftAssignment;

import java.util.List;
import java.util.UUID;

public interface ShiftAssignmentRepositoryPort {

    ShiftAssignment save(ShiftAssignment assignment);

    List<ShiftAssignment> findByUserAndTenant(UUID userId, UUID tenantId);
}
