package com.condor.nexussoft.timeclock.tenancy.domain.port.in;

import com.condor.nexussoft.timeclock.shared.domain.Paged;
import com.condor.nexussoft.timeclock.tenancy.domain.Company;
import com.condor.nexussoft.timeclock.tenancy.domain.CompanyStatus;

import java.util.UUID;

/** Puerto de entrada para la administración de empresas (RF-13). */
public interface CompanyManagementUseCase {

    Company create(CompanyCommands.CreateCompanyCommand command);

    Company update(UUID id, CompanyCommands.UpdateCompanyCommand command);

    Company changeStatus(UUID id, CompanyStatus status);

    Company get(UUID id);

    Paged<Company> list(int page, int size, String search);
}
