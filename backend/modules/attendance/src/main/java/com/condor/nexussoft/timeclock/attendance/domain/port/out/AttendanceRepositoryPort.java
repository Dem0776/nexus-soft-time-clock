package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceRecord;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceSummary;

import java.util.List;
import java.util.UUID;

public interface AttendanceRepositoryPort {

    void save(AttendanceRecord record);

    List<AttendanceSummary> findRecentByUser(UUID tenantId, UUID userId, int limit);
}
