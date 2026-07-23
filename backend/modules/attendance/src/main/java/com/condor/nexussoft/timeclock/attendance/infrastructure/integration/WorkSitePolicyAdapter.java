package com.condor.nexussoft.timeclock.attendance.infrastructure.integration;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.WorkSitePolicyPort;
import com.condor.nexussoft.timeclock.organization.domain.WorkSite;
import com.condor.nexussoft.timeclock.organization.domain.port.in.WorkSiteManagementUseCase;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Puente hacia Organization: obtiene la política de registro del centro (precisión, foto, biometría). */
@Component
public class WorkSitePolicyAdapter implements WorkSitePolicyPort {

    private final WorkSiteManagementUseCase workSites;

    public WorkSitePolicyAdapter(WorkSiteManagementUseCase workSites) {
        this.workSites = workSites;
    }

    @Override
    public SitePolicy find(UUID tenantId, UUID workSiteId) {
        // Un centro inexistente ya deriva en otros rechazos (QR/geocerca); aquí devolvemos una
        // política permisiva en lugar de lanzar, para no marcar la transacción como rollback-only.
        try {
            WorkSite site = workSites.get(tenantId, workSiteId);
            return new SitePolicy(site.gpsAccuracyMaxM(),
                    Boolean.TRUE.equals(site.requirePhoto()),
                    Boolean.TRUE.equals(site.requireBiometric()));
        } catch (ResourceNotFoundException e) {
            return SitePolicy.permissive();
        }
    }
}
