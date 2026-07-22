package com.condor.nexussoft.timeclock.organization.infrastructure.persistence;

import com.condor.nexussoft.timeclock.organization.domain.Project;
import com.condor.nexussoft.timeclock.organization.domain.port.out.ProjectRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProjectPersistenceAdapter implements ProjectRepositoryPort {

    private final ProjectJpaRepository jpa;

    public ProjectPersistenceAdapter(ProjectJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Project save(Project project) {
        jpa.save(toEntity(project));
        return project;
    }

    @Override
    public Project update(Project project) {
        ProjectJpaEntity e = jpa.findByIdAndTenantId(project.id(), project.tenantId()).orElseThrow();
        e.setName(project.name());
        e.setStatus(project.status().name());
        e.setStartsOn(project.startsOn());
        e.setEndsOn(project.endsOn());
        jpa.save(e);
        return project;
    }

    @Override
    public Optional<Project> findByIdAndTenant(UUID id, UUID tenantId) {
        return jpa.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public boolean existsByTenantAndCode(UUID tenantId, String code) {
        return jpa.existsByTenantIdAndCodeIgnoreCase(tenantId, code);
    }

    @Override
    public Paged<Project> findAllByTenant(UUID tenantId, int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProjectJpaEntity> result = (search == null || search.isBlank())
                ? jpa.findByTenantId(tenantId, pageable)
                : jpa.findByTenantIdAndNameContainingIgnoreCase(tenantId, search, pageable);
        return new Paged<>(result.map(this::toDomain).getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private ProjectJpaEntity toEntity(Project p) {
        return new ProjectJpaEntity(p.id(), p.tenantId(), p.code(), p.name(),
                p.status().name(), p.startsOn(), p.endsOn());
    }

    private Project toDomain(ProjectJpaEntity e) {
        return new Project(e.getId(), e.getTenantId(), e.getCode(), e.getName(),
                Project.Status.valueOf(e.getStatus()), e.getStartsOn(), e.getEndsOn());
    }
}
