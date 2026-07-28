package com.condor.nexussoft.timeclock.hr.application;

import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import com.condor.nexussoft.timeclock.hr.domain.VacationTier;
import com.condor.nexussoft.timeclock.hr.domain.port.in.VacationPolicyUseCase;
import com.condor.nexussoft.timeclock.hr.domain.port.out.VacationPolicyRepositoryPort;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta y actualización de la política de vacaciones del tenant (escalera por antigüedad). */
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
        if (c.tiers() == null || c.tiers().isEmpty()) {
            throw new IllegalArgumentException("Define al menos un tramo de antigüedad (año → días).");
        }
        Set<Integer> years = new HashSet<>();
        for (VacationTier t : c.tiers()) {
            if (t.year() < 1 || t.year() > 100) {
                throw new IllegalArgumentException("El año de antigüedad debe estar entre 1 y 100.");
            }
            if (t.days() < 0 || t.days() > 366) {
                throw new IllegalArgumentException("Los días de un tramo deben estar entre 0 y 366.");
            }
            if (!years.add(t.year())) {
                throw new IllegalArgumentException("Hay años de antigüedad repetidos en la escalera.");
            }
        }
        if (c.beyondIncrementDays() < 0 || c.beyondIncrementDays() > 366) {
            throw new IllegalArgumentException("El incremento debe estar entre 0 y 366.");
        }
        if (c.beyondEveryYears() < 1) {
            throw new IllegalArgumentException("El intervalo de años del incremento debe ser al menos 1.");
        }
        return repository.save(new VacationPolicy(
                c.tenantId(), c.tiers(), c.beyondMode(), c.beyondIncrementDays(), c.beyondEveryYears(),
                c.requireApproval(), c.countBusinessDaysOnly()));
    }
}
