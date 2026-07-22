package com.condor.nexussoft.timeclock.sync.domain.port.in;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceCommand;

import java.util.List;
import java.util.UUID;

/** Ingesta por lotes de registros offline (RF-21, CU-05). Procesa cada item de forma idempotente. */
public interface SyncAttendanceUseCase {

    List<SyncItemResult> sync(UUID tenantId, UUID userId, List<RegisterAttendanceCommand> commands);
}
