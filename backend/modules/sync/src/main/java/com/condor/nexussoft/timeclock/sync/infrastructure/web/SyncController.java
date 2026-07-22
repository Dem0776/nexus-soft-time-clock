package com.condor.nexussoft.timeclock.sync.infrastructure.web;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceCommand;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.RegisterAttendanceRequest;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.sync.domain.port.in.SyncAttendanceUseCase;
import com.condor.nexussoft.timeclock.sync.domain.port.in.SyncItemResult;
import com.condor.nexussoft.timeclock.sync.infrastructure.web.dto.SyncDtos.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Sincronización offline de asistencia (RF-21, CU-05). Devuelve el resultado por operación (RN-54). */
@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final SyncAttendanceUseCase sync;

    public SyncController(SyncAttendanceUseCase sync) {
        this.sync = sync;
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasAuthority('attendance:register')")
    public SyncResponse syncAttendance(@Valid @RequestBody SyncRequest request) {
        List<RegisterAttendanceCommand> commands = request.operations().stream()
                .map(RegisterAttendanceRequest::toCommand)
                .toList();
        List<SyncItemResult> results = sync.sync(TenantContext.require(), currentUserId(), commands);
        return new SyncResponse(results.stream().map(SyncItemResponse::from).toList());
    }

    private UUID currentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
