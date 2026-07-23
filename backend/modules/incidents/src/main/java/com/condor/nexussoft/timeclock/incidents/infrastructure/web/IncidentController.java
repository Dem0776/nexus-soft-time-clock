package com.condor.nexussoft.timeclock.incidents.infrastructure.web;

import com.condor.nexussoft.timeclock.incidents.domain.Incident;
import com.condor.nexussoft.timeclock.incidents.domain.port.in.IncidentManagementUseCase;
import com.condor.nexussoft.timeclock.incidents.infrastructure.persistence.IncidentUserNameQuery;
import com.condor.nexussoft.timeclock.incidents.infrastructure.web.dto.IncidentDtos.*;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Gestión de incidencias (RF-09). Requiere {@code incident:approve}. */
@RestController
@RequestMapping("/api/v1/incidents")
@PreAuthorize("hasAuthority('incident:approve')")
public class IncidentController {

    private final IncidentManagementUseCase incidents;
    private final IncidentUserNameQuery userNames;

    public IncidentController(IncidentManagementUseCase incidents, IncidentUserNameQuery userNames) {
        this.incidents = incidents;
        this.userNames = userNames;
    }

    @GetMapping
    public PageResponse<IncidentResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID tenantId = TenantContext.require();
        Paged<Incident> result = incidents.list(tenantId, status, page, size);
        List<UUID> userIds = result.items().stream().map(Incident::userId).distinct().toList();
        Map<UUID, String> names = userNames.namesByUserId(tenantId, userIds);
        return PageResponse.of(result.items().stream()
                        .map(i -> IncidentResponse.from(i, names.get(i.userId())))
                        .collect(Collectors.toList()),
                result.page(), result.size(), result.total());
    }

    @PatchMapping("/{id}/resolve")
    public IncidentResponse resolve(@PathVariable UUID id, @Valid @RequestBody ResolveIncidentRequest request) {
        UUID tenantId = TenantContext.require();
        Incident incident = incidents.resolve(tenantId, id,
                request.status(), request.note(), currentUserId());
        String userName = userNames.namesByUserId(tenantId, List.of(incident.userId())).get(incident.userId());
        return IncidentResponse.from(incident, userName);
    }

    private UUID currentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
