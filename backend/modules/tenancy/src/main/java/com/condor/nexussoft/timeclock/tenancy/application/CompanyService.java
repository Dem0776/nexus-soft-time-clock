package com.condor.nexussoft.timeclock.tenancy.application;

import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import com.condor.nexussoft.timeclock.tenancy.domain.Company;
import com.condor.nexussoft.timeclock.tenancy.domain.CompanyStatus;
import com.condor.nexussoft.timeclock.tenancy.domain.port.in.CompanyCommands;
import com.condor.nexussoft.timeclock.tenancy.domain.port.in.CompanyManagementUseCase;
import com.condor.nexussoft.timeclock.tenancy.domain.port.out.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CompanyService implements CompanyManagementUseCase {

    private final CompanyRepositoryPort companies;

    public CompanyService(CompanyRepositoryPort companies) {
        this.companies = companies;
    }

    @Override
    @Transactional
    public Company create(CompanyCommands.CreateCompanyCommand command) {
        if (companies.existsByCode(command.code())) {
            throw new DomainException("DUPLICATE_CODE", "Ya existe una empresa con el código " + command.code());
        }
        Company company = Company.create(command.code(), command.name(), command.legalName(),
                command.emailDomain(), command.timezone(), command.locale());
        return companies.save(company);
    }

    @Override
    @Transactional
    public Company update(UUID id, CompanyCommands.UpdateCompanyCommand command) {
        Company company = requireCompany(id);
        company.update(command.name(), command.legalName(), command.emailDomain(),
                command.timezone(), command.locale());
        return companies.update(company);
    }

    @Override
    @Transactional
    public Company changeStatus(UUID id, CompanyStatus status) {
        Company company = requireCompany(id);
        company.changeStatus(status);
        return companies.update(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Company get(UUID id) {
        return requireCompany(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<Company> list(int page, int size, String search) {
        return companies.findAll(page, size, search);
    }

    private Company requireCompany(UUID id) {
        return companies.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
    }
}
