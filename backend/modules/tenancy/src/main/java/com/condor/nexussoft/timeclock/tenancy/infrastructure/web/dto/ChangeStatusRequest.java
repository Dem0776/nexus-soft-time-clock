package com.condor.nexussoft.timeclock.tenancy.infrastructure.web.dto;

import jakarta.validation.constraints.Pattern;

public record ChangeStatusRequest(
        @Pattern(regexp = "ACTIVE|SUSPENDED|INACTIVE", message = "estado inválido") String status) {
}
