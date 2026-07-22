package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {
}
