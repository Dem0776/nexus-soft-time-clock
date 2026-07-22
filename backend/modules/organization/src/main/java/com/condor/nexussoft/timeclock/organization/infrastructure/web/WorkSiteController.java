package com.condor.nexussoft.timeclock.organization.infrastructure.web;

import com.condor.nexussoft.timeclock.organization.domain.WorkSite;
import com.condor.nexussoft.timeclock.organization.domain.port.in.WorkSiteCommands;
import com.condor.nexussoft.timeclock.organization.domain.port.in.WorkSiteManagementUseCase;
import com.condor.nexussoft.timeclock.organization.infrastructure.web.dto.*;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Administración de centros de trabajo (RF-07). Requiere {@code worksite:manage}; acotado al tenant. */
@RestController
@RequestMapping("/api/v1/work-sites")
@PreAuthorize("hasAuthority('worksite:manage')")
public class WorkSiteController {

    private final WorkSiteManagementUseCase workSites;

    public WorkSiteController(WorkSiteManagementUseCase workSites) {
        this.workSites = workSites;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkSiteResponse create(@Valid @RequestBody WorkSiteRequest r) {
        WorkSite site = workSites.create(tenant(), new WorkSiteCommands.CreateWorkSiteCommand(
                r.code(), r.name(), r.address(), r.latitude(), r.longitude(), r.timezone(),
                r.gpsAccuracyMaxM(), r.requirePhoto(), r.requireBiometric()));
        return WorkSiteResponse.from(site);
    }

    @GetMapping
    public PageResponse<WorkSiteResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Paged<WorkSite> result = workSites.list(tenant(), page, size, search);
        return PageResponse.of(result.items().stream().map(WorkSiteResponse::from).toList(),
                result.page(), result.size(), result.total());
    }

    @GetMapping("/{id}")
    public WorkSiteResponse get(@PathVariable UUID id) {
        return WorkSiteResponse.from(workSites.get(tenant(), id));
    }

    @PutMapping("/{id}")
    public WorkSiteResponse update(@PathVariable UUID id, @Valid @RequestBody WorkSiteUpdateRequest r) {
        WorkSite site = workSites.update(tenant(), id, new WorkSiteCommands.UpdateWorkSiteCommand(
                r.name(), r.address(), r.latitude(), r.longitude(), r.timezone(),
                r.gpsAccuracyMaxM(), r.requirePhoto(), r.requireBiometric()));
        return WorkSiteResponse.from(site);
    }

    @PatchMapping("/{id}/status")
    public WorkSiteResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody WorkSiteStatusRequest r) {
        return WorkSiteResponse.from(workSites.changeStatus(tenant(), id, WorkSite.Status.valueOf(r.status())));
    }

    private UUID tenant() {
        return TenantContext.require();
    }
}
