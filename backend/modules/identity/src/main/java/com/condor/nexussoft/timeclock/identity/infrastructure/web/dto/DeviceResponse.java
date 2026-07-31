package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase.DeviceView;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(UUID id, UUID userId, String deviceIdentifier, String platform, String model,
                             String osVersion, String status, boolean trusted,
                             Instant lastSeenAt, Instant createdAt) {

    public static DeviceResponse from(DeviceView v) {
        return new DeviceResponse(v.id(), v.userId(), v.deviceIdentifier(), v.platform(), v.model(),
                v.osVersion(), v.status().name(), v.trusted(), v.lastSeenAt(), v.createdAt());
    }
}
