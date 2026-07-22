package com.condor.nexussoft.timeclock.tenancy.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.tenancy.domain.Company;

import java.util.UUID;

public record CompanyResponse(UUID id, String code, String name, String legalName,
                              String emailDomain, String timezone, String locale, String status) {

    public static CompanyResponse from(Company c) {
        return new CompanyResponse(c.id(), c.code(), c.name(), c.legalName(),
                c.emailDomain(), c.timezone(), c.locale(), c.status().name());
    }
}
