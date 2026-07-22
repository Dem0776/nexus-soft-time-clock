package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import jakarta.validation.constraints.*;

/** Alta de centro de trabajo. */
public record WorkSiteRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 400) String address,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        String timezone,
        @Positive Integer gpsAccuracyMaxM,
        Boolean requirePhoto,
        Boolean requireBiometric) {
}
