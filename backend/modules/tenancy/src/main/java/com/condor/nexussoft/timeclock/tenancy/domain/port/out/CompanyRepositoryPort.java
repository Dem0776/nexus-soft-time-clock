package com.condor.nexussoft.timeclock.tenancy.domain.port.out;

import com.condor.nexussoft.timeclock.shared.domain.Paged;
import com.condor.nexussoft.timeclock.tenancy.domain.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepositoryPort {

    /** Persiste una empresa nueva (incluye su configuración por defecto). */
    Company save(Company company);

    Company update(Company company);

    Optional<Company> findById(UUID id);

    boolean existsByCode(String code);

    Paged<Company> findAll(int page, int size, String search);
}
