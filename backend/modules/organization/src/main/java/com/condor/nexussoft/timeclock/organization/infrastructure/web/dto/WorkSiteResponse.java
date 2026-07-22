package com.condor.nexussoft.timeclock.organization.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.organization.domain.WorkSite;

import java.util.UUID;

public record WorkSiteResponse(UUID id, String code, String name, String address,
                               double latitude, double longitude, String timezone,
                               Integer gpsAccuracyMaxM, Boolean requirePhoto, Boolean requireBiometric,
                               String status) {

    public static WorkSiteResponse from(WorkSite s) {
        return new WorkSiteResponse(s.id(), s.code(), s.name(), s.address(),
                s.location().latitude(), s.location().longitude(), s.timezone(),
                s.gpsAccuracyMaxM(), s.requirePhoto(), s.requireBiometric(), s.status().name());
    }
}
