package com.condor.nexussoft.timeclock.organization.application;

import com.condor.nexussoft.timeclock.organization.domain.Project;
import com.condor.nexussoft.timeclock.organization.domain.port.in.ProjectCommands;
import com.condor.nexussoft.timeclock.organization.domain.port.in.ProjectManagementUseCase;
import com.condor.nexussoft.timeclock.organization.domain.port.out.ProjectRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProjectService implements ProjectManagementUseCase {

    private final ProjectRepositoryPort projects;

    public ProjectService(ProjectRepositoryPort projects) {
        this.projects = projects;
    }

    @Override
    @Transactional
    public Project create(UUID tenantId, ProjectCommands.CreateProjectCommand c) {
        if (projects.existsByTenantAndCode(tenantId, c.code())) {
            throw new DomainException("DUPLICATE_CODE", "Ya existe un proyecto con el código " + c.code());
        }
        return projects.save(Project.create(tenantId, c.code(), c.name(), c.startsOn(), c.endsOn()));
    }

    @Override
    @Transactional
    public Project update(UUID tenantId, UUID id, ProjectCommands.UpdateProjectCommand c) {
        Project project = requireProject(tenantId, id);
        Project.Status status = c.status() == null ? null : Project.Status.valueOf(c.status());
        project.update(c.name(), status, c.startsOn(), c.endsOn());
        return projects.update(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Project get(UUID tenantId, UUID id) {
        return requireProject(tenantId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<Project> list(UUID tenantId, int page, int size, String search) {
        return projects.findAllByTenant(tenantId, page, size, search);
    }

    private Project requireProject(UUID tenantId, UUID id) {
        return projects.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", id));
    }
}
