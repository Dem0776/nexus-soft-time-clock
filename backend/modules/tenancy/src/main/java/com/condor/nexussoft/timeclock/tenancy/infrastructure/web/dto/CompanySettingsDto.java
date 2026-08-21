package com.condor.nexussoft.timeclock.tenancy.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.tenancy.domain.CompanySettings;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/** Políticas por defecto del tenant tal como las edita el portal. */
public record CompanySettingsDto(
        @Min(5) @Max(500) Integer defaultGpsAccuracyMaxM,
        boolean requirePhoto,
        boolean requireBiometric,
        boolean deviceBindingEnabled,
        @Pattern(regexp = "REJECT|FLAG") String deviceBindingAction) {

    public static CompanySettingsDto from(CompanySettings s) {
        return new CompanySettingsDto(
                s.defaultGpsAccuracyMaxM(),
                s.requirePhoto(),
                s.requireBiometric(),
                s.deviceBindingEnabled(),
                s.deviceBindingAction().name());
    }
}
