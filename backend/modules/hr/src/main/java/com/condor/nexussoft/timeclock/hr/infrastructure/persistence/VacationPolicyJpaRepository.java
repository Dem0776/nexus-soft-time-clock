package com.condor.nexussoft.timeclock.hr.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacationPolicyJpaRepository extends JpaRepository<VacationPolicyJpaEntity, UUID> {
}
