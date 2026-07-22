package com.condor.nexussoft.timeclock.tenancy.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Configuración por defecto del tenant. Solo se mapea la clave; el resto de columnas
 * usa los valores por defecto de la BD (ver migración V1) al insertar.
 */
@Entity
@Table(name = "company_settings")
public class CompanySettingsJpaEntity {

    @Id
    @Column(name = "company_id")
    private UUID companyId;

    protected CompanySettingsJpaEntity() {
    }

    public CompanySettingsJpaEntity(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getCompanyId() {
        return companyId;
    }
}
