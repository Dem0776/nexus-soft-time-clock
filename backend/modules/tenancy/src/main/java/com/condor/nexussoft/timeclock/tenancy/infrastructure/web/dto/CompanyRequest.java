package com.condor.nexussoft.timeclock.tenancy.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta de empresa. */
public record CompanyRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 255) String legalName,
        @Size(max = 255) String emailDomain,
        String timezone,
        String locale) {
}
