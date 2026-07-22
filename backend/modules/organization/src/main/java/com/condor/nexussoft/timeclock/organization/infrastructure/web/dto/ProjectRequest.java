package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 200) String name,
        LocalDate startsOn,
        LocalDate endsOn) {
}
