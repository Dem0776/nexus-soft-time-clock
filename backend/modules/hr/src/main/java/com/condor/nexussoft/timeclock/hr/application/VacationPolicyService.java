package com.condor.nexussoft.timeclock.hr.application;

import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import com.condor.nexussoft.timeclock.hr.domain.port.in.VacationPolicyUseCase;
import com.condor.nexussoft.timeclock.hr.domain.port.out.VacationPolicyRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta y actualización de la política de vacaciones del tenant. */
@Service
public class VacationPolicyService implements VacationPolicyUseCase {

    private final VacationPolicyRepositoryPort repository;

    public VacationPolicyService(VacationPolicyRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public VacationPolicy get(UUID tenantId) {
        return repository.findByTenant(tenantId).orElseGet(() -> VacationPolicy.defaults(tenantId));
    }

    @Override
    @Transactional
    public VacationPolicy update(UpdatePolicyCommand c) {
        if (c.daysPerYear() < 0 || c.daysPerYear() > 366) {
            throw new IllegalArgumentException("daysPerYear debe estar entre 0 y 366");
        }
        return repository.save(new VacationPolicy(
                c.tenantId(), c.daysPerYear(), c.requireApproval(), c.countBusinessDaysOnly()));
    }
}
