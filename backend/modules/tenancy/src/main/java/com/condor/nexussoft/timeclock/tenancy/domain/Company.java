package com.condor.nexussoft.timeclock.tenancy.domain;

import java.util.UUID;

/** Agregado raíz de empresa (tenant). Encapsula sus datos y estado de ciclo de vida. */
public class Company {

    private final UUID id;
    private final String code;
    private String name;
    private String legalName;
    private String emailDomain;
    private String timezone;
    private String locale;
    private CompanyStatus status;

    public Company(UUID id, String code, String name, String legalName, String emailDomain,
                   String timezone, String locale, CompanyStatus status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.legalName = legalName;
        this.emailDomain = emailDomain;
        this.timezone = timezone;
        this.locale = locale;
        this.status = status;
    }

    /** Fábrica de creación: estado ACTIVE por defecto. */
    public static Company create(String code, String name, String legalName, String emailDomain,
                                 String timezone, String locale) {
        return new Company(UUID.randomUUID(), code, name, legalName, emailDomain,
                timezone == null || timezone.isBlank() ? "UTC" : timezone,
                locale == null || locale.isBlank() ? "es" : locale,
                CompanyStatus.ACTIVE);
    }

    public void update(String name, String legalName, String emailDomain, String timezone, String locale) {
        this.name = name;
        this.legalName = legalName;
        this.emailDomain = emailDomain;
        if (timezone != null && !timezone.isBlank()) this.timezone = timezone;
        if (locale != null && !locale.isBlank()) this.locale = locale;
    }

    public void changeStatus(CompanyStatus status) {
        this.status = status;
    }

    public UUID id() { return id; }
    public String code() { return code; }
    public String name() { return name; }
    public String legalName() { return legalName; }
    public String emailDomain() { return emailDomain; }
    public String timezone() { return timezone; }
    public String locale() { return locale; }
    public CompanyStatus status() { return status; }
}
