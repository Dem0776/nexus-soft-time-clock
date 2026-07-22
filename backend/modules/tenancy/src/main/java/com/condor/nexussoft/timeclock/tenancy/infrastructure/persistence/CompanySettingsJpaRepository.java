package com.condor.nexussoft.timeclock.tenancy.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanySettingsJpaRepository extends JpaRepository<CompanySettingsJpaEntity, UUID> {
}
