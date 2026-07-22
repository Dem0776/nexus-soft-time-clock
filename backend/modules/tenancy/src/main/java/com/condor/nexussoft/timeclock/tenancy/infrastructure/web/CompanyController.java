package com.condor.nexussoft.timeclock.tenancy.infrastructure.web;

import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import com.condor.nexussoft.timeclock.tenancy.domain.Company;
import com.condor.nexussoft.timeclock.tenancy.domain.CompanyStatus;
import com.condor.nexussoft.timeclock.tenancy.domain.port.in.CompanyCommands;
import com.condor.nexussoft.timeclock.tenancy.domain.port.in.CompanyManagementUseCase;
import com.condor.nexussoft.timeclock.tenancy.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Administración de empresas (RF-13). Requiere el permiso {@code company:manage} (SUPER_ADMIN). */
@RestController
@RequestMapping("/api/v1/companies")
@PreAuthorize("hasAuthority('company:manage')")
public class CompanyController {

    private final CompanyManagementUseCase companies;

    public CompanyController(CompanyManagementUseCase companies) {
        this.companies = companies;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyRequest request) {
        Company company = companies.create(new CompanyCommands.CreateCompanyCommand(
                request.code(), request.name(), request.legalName(),
                request.emailDomain(), request.timezone(), request.locale()));
        return CompanyResponse.from(company);
    }

    @GetMapping
    public PageResponse<CompanyResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Paged<Company> result = companies.list(page, size, search);
        return PageResponse.of(
                result.items().stream().map(CompanyResponse::from).toList(),
                result.page(), result.size(), result.total());
    }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable UUID id) {
        return CompanyResponse.from(companies.get(id));
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyUpdateRequest request) {
        Company company = companies.update(id, new CompanyCommands.UpdateCompanyCommand(
                request.name(), request.legalName(), request.emailDomain(),
                request.timezone(), request.locale()));
        return CompanyResponse.from(company);
    }

    @PatchMapping("/{id}/status")
    public CompanyResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return CompanyResponse.from(companies.changeStatus(id, CompanyStatus.valueOf(request.status())));
    }
}
