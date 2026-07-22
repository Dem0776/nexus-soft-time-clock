package com.condor.nexussoft.timeclock.tenancy.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Page<CompanyJpaEntity> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
