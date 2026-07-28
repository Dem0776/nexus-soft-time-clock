package com.condor.nexussoft.timeclock.hr.infrastructure.persistence;

import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import com.condor.nexussoft.timeclock.hr.domain.port.out.VacationPolicyRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VacationPolicyPersistenceAdapter implements VacationPolicyRepositoryPort {

    private final VacationPolicyJpaRepository repository;

    public VacationPolicyPersistenceAdapter(VacationPolicyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<VacationPolicy> findByTenant(UUID tenantId) {
        return repository.findById(tenantId).map(VacationPolicyPersistenceAdapter::toDomain);
    }

    @Override
    public VacationPolicy save(VacationPolicy p) {
        VacationPolicyJpaEntity e = repository.findById(p.tenantId()).orElseGet(VacationPolicyJpaEntity::new);
        e.setTenantId(p.tenantId());
        e.setDaysPerYear(p.daysPerYear());
        e.setRequireApproval(p.requireApproval());
        e.setCountBusinessDaysOnly(p.countBusinessDaysOnly());
        return toDomain(repository.save(e));
    }

    static VacationPolicy toDomain(VacationPolicyJpaEntity e) {
        return new VacationPolicy(e.getTenantId(), e.getDaysPerYear(), e.isRequireApproval(), e.isCountBusinessDaysOnly());
    }
}
