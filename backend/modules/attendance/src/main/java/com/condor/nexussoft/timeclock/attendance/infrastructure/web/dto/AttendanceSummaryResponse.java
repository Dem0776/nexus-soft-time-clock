package com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceSummary;

import java.time.Instant;
import java.util.UUID;

public record AttendanceSummaryResponse(UUID id, String eventType, String status, String rejectionReason,
                                        Instant serverTime, double latitude, double longitude) {

    public static AttendanceSummaryResponse from(AttendanceSummary s) {
        return new AttendanceSummaryResponse(s.id(), s.eventType(), s.status(), s.rejectionReason(),
                s.serverTime(), s.latitude(), s.longitude());
    }
}
