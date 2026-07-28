package com.condor.nexussoft.timeclock.hr.infrastructure.persistence;

import com.condor.nexussoft.timeclock.hr.domain.VacationPolicy;
import com.condor.nexussoft.timeclock.hr.domain.VacationTier;
import com.condor.nexussoft.timeclock.hr.domain.port.out.VacationPolicyRepositoryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VacationPolicyPersistenceAdapter implements VacationPolicyRepositoryPort {

    private static final TypeReference<List<VacationTier>> TIER_LIST = new TypeReference<>() {};

    private final VacationPolicyJpaRepository repository;
    private final ObjectMapper mapper;

    public VacationPolicyPersistenceAdapter(VacationPolicyJpaRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<VacationPolicy> findByTenant(UUID tenantId) {
        return repository.findById(tenantId).map(this::toDomain);
    }

    @Override
    public VacationPolicy save(VacationPolicy p) {
        VacationPolicyJpaEntity e = repository.findById(p.tenantId()).orElseGet(VacationPolicyJpaEntity::new);
        e.setTenantId(p.tenantId());
        e.setTiersJson(writeTiers(p.tiers()));
        e.setBeyondMode(p.beyondMode());
        e.setBeyondIncrementDays(p.beyondIncrementDays());
        e.setBeyondEveryYears(p.beyondEveryYears());
        e.setRequireApproval(p.requireApproval());
        e.setCountBusinessDaysOnly(p.countBusinessDaysOnly());
        return toDomain(repository.save(e));
    }

    private VacationPolicy toDomain(VacationPolicyJpaEntity e) {
        return new VacationPolicy(e.getTenantId(), readTiers(e.getTiersJson()), e.getBeyondMode(),
                e.getBeyondIncrementDays(), e.getBeyondEveryYears(), e.isRequireApproval(), e.isCountBusinessDaysOnly());
    }

    private List<VacationTier> readTiers(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return mapper.readValue(json, TIER_LIST);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo leer la escalera de vacaciones almacenada.", ex);
        }
    }

    private String writeTiers(List<VacationTier> tiers) {
        try {
            return mapper.writeValueAsString(tiers == null ? List.of() : tiers);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo serializar la escalera de vacaciones.", ex);
        }
    }
}
