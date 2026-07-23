package com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceEventType;
import com.condor.nexussoft.timeclock.attendance.domain.EventTypeSetting;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO de un tipo de evento configurable (HU-12 CA1). */
public record EventTypeSettingDto(
        @NotNull AttendanceEventType eventType,
        boolean enabled,
        @Size(max = 60) String label) {

    public static EventTypeSettingDto from(EventTypeSetting s) {
        return new EventTypeSettingDto(s.eventType(), s.enabled(), s.label());
    }

    public EventTypeSetting toDomain() {
        return new EventTypeSetting(eventType, enabled, label);
    }
}
