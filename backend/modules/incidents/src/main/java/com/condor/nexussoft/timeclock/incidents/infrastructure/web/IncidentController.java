package com.condor.nexussoft.timeclock.incidents.infrastructure.web;

import com.condor.nexussoft.timeclock.incidents.domain.Incident;
import com.condor.nexussoft.timeclock.incidents.domain.port.in.IncidentManagementUseCase;
import com.condor.nexussoft.timeclock.incidents.infrastructure.web.dto.IncidentDtos.*;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Gestión de incidencias (RF-09). Requiere {@code incident:approve}. */
@RestController
@RequestMapping("/api/v1/incidents")
@PreAuthorize("hasAuthority('incident:approve')")
public class IncidentController {

    private final IncidentManagementUseCase incidents;

    public IncidentController(IncidentManagementUseCase incidents) {
        this.incidents = incidents;
    }

    @GetMapping
    public PageResponse<IncidentResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Paged<Incident> result = incidents.list(TenantContext.require(), status, page, size);
        return PageResponse.of(result.items().stream().map(IncidentResponse::from).toList(),
                result.page(), result.size(), result.total());
    }

    @PatchMapping("/{id}/resolve")
    public IncidentResponse resolve(@PathVariable UUID id, @Valid @RequestBody ResolveIncidentRequest request) {
        Incident incident = incidents.resolve(TenantContext.require(), id,
                request.status(), request.note(), currentUserId());
        return IncidentResponse.from(incident);
    }

    private UUID currentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
