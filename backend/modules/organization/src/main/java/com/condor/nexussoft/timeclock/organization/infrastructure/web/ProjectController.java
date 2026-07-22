package com.condor.nexussoft.timeclock.organization.infrastructure.web;

import com.condor.nexussoft.timeclock.organization.domain.Project;
import com.condor.nexussoft.timeclock.organization.domain.port.in.ProjectCommands;
import com.condor.nexussoft.timeclock.organization.domain.port.in.ProjectManagementUseCase;
import com.condor.nexussoft.timeclock.organization.infrastructure.web.dto.*;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Administración de proyectos (RF-23). Requiere {@code project:manage}; acotado al tenant. */
@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("hasAuthority('project:manage')")
public class ProjectController {

    private final ProjectManagementUseCase projects;

    public ProjectController(ProjectManagementUseCase projects) {
        this.projects = projects;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest r) {
        Project project = projects.create(tenant(),
                new ProjectCommands.CreateProjectCommand(r.code(), r.name(), r.startsOn(), r.endsOn()));
        return ProjectResponse.from(project);
    }

    @GetMapping
    public PageResponse<ProjectResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Paged<Project> result = projects.list(tenant(), page, size, search);
        return PageResponse.of(result.items().stream().map(ProjectResponse::from).toList(),
                result.page(), result.size(), result.total());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable UUID id) {
        return ProjectResponse.from(projects.get(tenant(), id));
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectUpdateRequest r) {
        Project project = projects.update(tenant(),
                id, new ProjectCommands.UpdateProjectCommand(r.name(), r.status(), r.startsOn(), r.endsOn()));
        return ProjectResponse.from(project);
    }

    private UUID tenant() {
        return TenantContext.require();
    }
}
