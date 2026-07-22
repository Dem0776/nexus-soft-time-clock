package com.condor.nexussoft.timeclock.tenancy.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Actualización de empresa (el código es inmutable). */
public record CompanyUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 255) String legalName,
        @Size(max = 255) String emailDomain,
        String timezone,
        String locale) {
}
