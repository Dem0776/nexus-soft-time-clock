package com.condor.nexussoft.timeclock.tenancy.domain.port.in;

/** Comandos de creación/actualización de empresa. */
public final class CompanyCommands {

    private CompanyCommands() {
    }

    public record CreateCompanyCommand(String code, String name, String legalName,
                                       String emailDomain, String timezone, String locale) {
    }

    public record UpdateCompanyCommand(String name, String legalName,
                                       String emailDomain, String timezone, String locale) {
    }
}
