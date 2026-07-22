package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import jakarta.validation.constraints.Pattern;

public record WorkSiteStatusRequest(
        @Pattern(regexp = "ACTIVE|INACTIVE", message = "estado inválido") String status) {
}
