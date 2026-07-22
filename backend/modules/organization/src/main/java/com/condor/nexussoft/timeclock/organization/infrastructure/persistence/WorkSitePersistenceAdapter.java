package com.condor.nexussoft.timeclock.organization.infrastructure.persistence;

import com.condor.nexussoft.timeclock.organization.domain.WorkSite;
import com.condor.nexussoft.timeclock.organization.domain.port.out.WorkSiteRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class WorkSitePersistenceAdapter implements WorkSiteRepositoryPort {

    private final WorkSiteJpaRepository jpa;

    public WorkSitePersistenceAdapter(WorkSiteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public WorkSite save(WorkSite site) {
        jpa.save(toEntity(site));
        return site;
    }

    @Override
    public WorkSite update(WorkSite site) {
        WorkSiteJpaEntity e = jpa.findByIdAndTenantId(site.id(), site.tenantId()).orElseThrow();
        e.setName(site.name());
        e.setAddress(site.address());
        e.setLocation(GeoSupport.toPoint(site.location()));
        e.setTimezone(site.timezone());
        e.setGpsAccuracyMaxM(site.gpsAccuracyMaxM());
        e.setRequirePhoto(site.requirePhoto());
        e.setRequireBiometric(site.requireBiometric());
        e.setStatus(site.status().name());
        jpa.save(e);
        return site;
    }

    @Override
    public Optional<WorkSite> findByIdAndTenant(UUID id, UUID tenantId) {
        return jpa.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public boolean existsByTenantAndCode(UUID tenantId, String code) {
        return jpa.existsByTenantIdAndCodeIgnoreCase(tenantId, code);
    }

    @Override
    public Paged<WorkSite> findAllByTenant(UUID tenantId, int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<WorkSiteJpaEntity> result = (search == null || search.isBlank())
                ? jpa.findByTenantId(tenantId, pageable)
                : jpa.findByTenantIdAndNameContainingIgnoreCase(tenantId, search, pageable);
        return new Paged<>(result.map(this::toDomain).getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private WorkSiteJpaEntity toEntity(WorkSite s) {
        return new WorkSiteJpaEntity(s.id(), s.tenantId(), s.code(), s.name(), s.address(),
                GeoSupport.toPoint(s.location()), s.timezone(), s.gpsAccuracyMaxM(),
                s.requirePhoto(), s.requireBiometric(), s.status().name());
    }

    private WorkSite toDomain(WorkSiteJpaEntity e) {
        return new WorkSite(e.getId(), e.getTenantId(), e.getCode(), e.getName(), e.getAddress(),
                GeoSupport.toGeoPoint(e.getLocation()), e.getTimezone(), e.getGpsAccuracyMaxM(),
                e.getRequirePhoto(), e.getRequireBiometric(), WorkSite.Status.valueOf(e.getStatus()));
    }
}
