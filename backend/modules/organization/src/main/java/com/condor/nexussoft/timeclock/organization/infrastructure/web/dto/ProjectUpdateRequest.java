package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Pattern(regexp = "ACTIVE|INACTIVE|CLOSED", message = "estado inválido") String status,
        LocalDate startsOn,
        LocalDate endsOn) {
}
