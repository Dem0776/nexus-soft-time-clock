package com.condor.nexussoft.timeclock.attendance.infrastructure.integration;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.CompanyPolicyPort;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.WorkSitePolicyPort;
import com.condor.nexussoft.timeclock.organization.domain.WorkSite;
import com.condor.nexussoft.timeclock.organization.domain.port.in.WorkSiteManagementUseCase;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resuelve la política de registro efectiva de un centro (precisión, foto, biometría).
 *
 * <p>Los overrides del centro son <b>tri-estado</b>: {@code NULL} significa heredar de
 * {@code company_settings} (así lo documenta la migración V3), no «desactivado». Tratar el nulo
 * como {@code false} dejaría la política de empresa sin efecto alguno.
 */
@Component
public class WorkSitePolicyAdapter implements WorkSitePolicyPort {

    private final WorkSiteManagementUseCase workSites;
    private final CompanyPolicyPort companyPolicy;

    public WorkSitePolicyAdapter(WorkSiteManagementUseCase workSites, CompanyPolicyPort companyPolicy) {
        this.workSites = workSites;
        this.companyPolicy = companyPolicy;
    }

    @Override
    public SitePolicy find(UUID tenantId, UUID workSiteId) {
        CompanyPolicyPort.CompanyPolicy company = companyPolicy.find(tenantId);
        try {
            WorkSite site = workSites.get(tenantId, workSiteId);
            return new SitePolicy(
                    site.gpsAccuracyMaxM() != null ? site.gpsAccuracyMaxM() : company.defaultGpsAccuracyMaxM(),
                    site.requirePhoto() != null ? site.requirePhoto() : company.requirePhoto(),
                    site.requireBiometric() != null ? site.requireBiometric() : company.requireBiometric());
        } catch (ResourceNotFoundException e) {
            // Un centro inexistente ya deriva en otros rechazos (QR/geocerca). Se devuelve la política
            // de la empresa —no una permisiva— para que un id inválido no relaje sus exigencias, y se
            // evita lanzar aquí para no marcar la transacción como rollback-only.
            return new SitePolicy(company.defaultGpsAccuracyMaxM(),
                    company.requirePhoto(), company.requireBiometric());
        }
    }
}
