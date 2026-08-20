package com.condor.nexussoft.timeclock.attendance.application;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.EvidenceUseCase;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.AttendanceEvidenceLookupPort;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.EvidenceStoragePort;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.WorkSitePolicyPort;
import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Emite tickets de subida y URLs de lectura de la evidencia fotográfica (RF-18, HU-13).
 * El backend no transporta binarios: solo firma URLs de vida corta (ADR-008).
 */
@Service
public class EvidenceService implements EvidenceUseCase {

    private final EvidenceStoragePort storage;
    private final AttendanceEvidenceLookupPort lookup;
    private final WorkSitePolicyPort sitePolicy;
    private final Clock clock;

    public EvidenceService(EvidenceStoragePort storage, AttendanceEvidenceLookupPort lookup,
                           WorkSitePolicyPort sitePolicy, Clock clock) {
        this.storage = storage;
        this.lookup = lookup;
        this.sitePolicy = sitePolicy;
        this.clock = clock;
    }

    @Override
    public EvidenceStoragePort.UploadTicket requestUpload(UUID tenantId, UUID userId, UUID workSiteId,
                                                          String contentType, long sizeBytes) {
        // Filtro barato de entrada. No sustituye a la verificación contra el objeto al registrar:
        // aquí solo se comprueba lo que el cliente *declara*.
        if (!storage.isContentTypeAllowed(contentType)) {
            throw new DomainException("UNSUPPORTED_MEDIA_TYPE",
                    "Formato de imagen no admitido: " + contentType);
        }
        if (sizeBytes > storage.maxBytes()) {
            throw new DomainException("EVIDENCE_TOO_LARGE",
                    "La imagen supera el máximo permitido de " + storage.maxBytes() + " bytes.");
        }
        return storage.presignUpload(tenantId, userId, workSiteId, clock.instant());
    }

    @Override
    public String viewUrl(UUID tenantId, UUID recordId, Instant serverTime) {
        String key = lookup.findEvidenceKey(tenantId, recordId, serverTime)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia", recordId));
        return storage.presignDownload(key);
    }

    @Override
    public WorkSitePolicyPort.SitePolicy sitePolicy(UUID tenantId, UUID workSiteId) {
        return sitePolicy.find(tenantId, workSiteId);
    }
}
