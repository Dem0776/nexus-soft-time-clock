package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(
        @Pattern(regexp = "ACTIVE|INACTIVE|LOCKED|INVITED", message = "estado inválido") String status) {
}
