package com.condor.nexussoft.timeclock.tenancy.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "companies")
public class CompanyJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "email_domain")
    private String emailDomain;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false)
    private String status;

    protected CompanyJpaEntity() {
    }

    public CompanyJpaEntity(UUID id, String code, String name, String legalName, String emailDomain,
                            String timezone, String locale, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.legalName = legalName;
        this.emailDomain = emailDomain;
        this.timezone = timezone;
        this.locale = locale;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getLegalName() { return legalName; }
    public String getEmailDomain() { return emailDomain; }
    public String getTimezone() { return timezone; }
    public String getLocale() { return locale; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public void setEmailDomain(String emailDomain) { this.emailDomain = emailDomain; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setLocale(String locale) { this.locale = locale; }
    public void setStatus(String status) { this.status = status; }
}
