package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import jakarta.validation.constraints.*;

/** Actualización de centro de trabajo (el código es inmutable). */
public record WorkSiteUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 400) String address,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        String timezone,
        @Positive Integer gpsAccuracyMaxM,
        Boolean requirePhoto,
        Boolean requireBiometric) {
}
