package com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttendanceResponse(UUID recordId, String status, String rejectionReason,
                                 Instant serverTime, Double distanceToSiteM, List<String> flags) {

    public static AttendanceResponse from(AttendanceResult r) {
        return new AttendanceResponse(r.recordId(), r.status(), r.rejectionReason(),
                r.serverTime(), r.distanceToSiteM(), r.flags());
    }
}
