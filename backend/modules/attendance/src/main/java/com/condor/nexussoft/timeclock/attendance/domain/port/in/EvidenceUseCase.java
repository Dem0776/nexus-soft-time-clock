package com.condor.nexussoft.timeclock.attendance.domain.port.in;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.EvidenceStoragePort;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.WorkSitePolicyPort;

import java.time.Instant;
import java.util.UUID;

/** Alta y consulta de evidencias fotográficas asociadas a registros de asistencia (RF-18). */
public interface EvidenceUseCase {

    /** Emite el ticket de subida directa al object storage para el usuario autenticado. */
    EvidenceStoragePort.UploadTicket requestUpload(UUID tenantId, UUID userId, UUID workSiteId,
                                                   String contentType, long sizeBytes);

    /** URL de lectura de vida corta de la evidencia de un registro del tenant. */
    String viewUrl(UUID tenantId, UUID recordId, Instant serverTime);

    /** Política de registro efectiva del centro, con la herencia empresa → centro ya resuelta. */
    WorkSitePolicyPort.SitePolicy sitePolicy(UUID tenantId, UUID workSiteId);
}
