package com.condor.nexussoft.timeclock.scheduling.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.scheduling.domain.Schedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** DTOs de horarios. */
public final class ScheduleDtos {

    private ScheduleDtos() {
    }

    public record ScheduleRequest(
            @NotBlank @Size(max = 40) String code,
            @NotBlank @Size(max = 200) String name,
            String timezone) {
    }

    public record ScheduleUpdateRequest(
            @NotBlank @Size(max = 200) String name,
            String timezone,
            @Pattern(regexp = "ACTIVE|INACTIVE", message = "estado inválido") String status) {
    }

    public record ScheduleResponse(UUID id, String code, String name, String timezone, String status) {
        public static ScheduleResponse from(Schedule s) {
            return new ScheduleResponse(s.id(), s.code(), s.name(), s.timezone(), s.status().name());
        }
    }
}
