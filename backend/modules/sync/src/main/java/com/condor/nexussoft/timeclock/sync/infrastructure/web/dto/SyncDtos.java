package com.condor.nexussoft.timeclock.sync.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.RegisterAttendanceRequest;
import com.condor.nexussoft.timeclock.sync.domain.port.in.SyncItemResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SyncDtos {

    private SyncDtos() {
    }

    /** Lote de operaciones offline (FIFO). Límite defensivo de tamaño de lote. */
    public record SyncRequest(
            @NotEmpty @Size(max = 200) List<@Valid RegisterAttendanceRequest> operations) {
    }

    public record SyncItemResponse(UUID operationUuid, String status, String rejectionReason,
                                   Instant serverTime, Double distanceToSiteM, List<String> flags, String error) {
        public static SyncItemResponse from(SyncItemResult r) {
            return new SyncItemResponse(r.operationUuid(), r.status(), r.rejectionReason(),
                    r.serverTime(), r.distanceToSiteM(), r.flags(), r.error());
        }
    }

    public record SyncResponse(List<SyncItemResponse> results) {
    }
}
