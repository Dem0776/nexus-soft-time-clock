package com.condor.nexussoft.timeclock.attendance.infrastructure.web;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.EvidenceUseCase;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.EvidenceDtos.EvidenceUploadRequest;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.EvidenceDtos.EvidenceUploadResponse;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.EvidenceDtos.EvidenceViewResponse;
import com.condor.nexussoft.timeclock.attendance.infrastructure.web.dto.EvidenceDtos.SitePolicyResponse;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Evidencia fotográfica (RF-18, HU-13). La subida es directa del cliente al object storage con una
 * URL prefirmada de vida corta (ADR-008): el binario nunca atraviesa el backend.
 *
 * <p>La clave del objeto la decide el servidor al emitir el ticket, y se verifica al registrar la
 * asistencia. Un cliente no puede inventarse una clave para dar por cumplida la exigencia de foto.
 */
@RestController
@RequestMapping("/api/v1/attendance")
public class EvidenceController {

    private final EvidenceUseCase evidence;

    public EvidenceController(EvidenceUseCase evidence) {
        this.evidence = evidence;
    }

    /** Ticket de subida para una evidencia del colaborador autenticado. */
    @PostMapping("/evidence/uploads")
    @PreAuthorize("hasAuthority('attendance:register')")
    public EvidenceUploadResponse requestUpload(@Valid @RequestBody EvidenceUploadRequest request) {
        return EvidenceUploadResponse.from(evidence.requestUpload(
                TenantContext.require(), currentUserId(),
                request.workSiteId(), request.contentType(), request.sizeBytes()));
    }

    /**
     * URL de lectura de la evidencia de un registro. {@code serverTime} es opcional pero conviene
     * enviarlo: la tabla está particionada por esa columna y acota la consulta a una partición.
     */
    @GetMapping("/evidence/{recordId}/url")
    @PreAuthorize("hasAnyAuthority('attendance:read','report:export')")
    public EvidenceViewResponse viewUrl(@PathVariable UUID recordId,
                                        @RequestParam(required = false) Instant serverTime) {
        return new EvidenceViewResponse(evidence.viewUrl(TenantContext.require(), recordId, serverTime));
    }

    /**
     * Política de registro efectiva del centro. La app la consulta para exigir la foto antes de
     * enviar, en lugar de descubrirlo por el rechazo del servidor (que sigue siendo autoritativo).
     */
    @GetMapping("/site-policy/{workSiteId}")
    @PreAuthorize("hasAuthority('attendance:register')")
    public SitePolicyResponse sitePolicy(@PathVariable UUID workSiteId) {
        return SitePolicyResponse.from(evidence.sitePolicy(TenantContext.require(), workSiteId));
    }

    private UUID currentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
