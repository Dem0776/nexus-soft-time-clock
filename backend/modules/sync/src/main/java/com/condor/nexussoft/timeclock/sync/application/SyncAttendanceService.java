package com.condor.nexussoft.timeclock.sync.application;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceResult;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceCommand;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.sync.domain.port.in.SyncAttendanceUseCase;
import com.condor.nexussoft.timeclock.sync.domain.port.in.SyncItemResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Procesa un lote de operaciones offline delegando cada una en el núcleo de asistencia
 * (idempotente, ADR-004). Cada item se procesa de forma aislada: un fallo no aborta el lote,
 * de modo que el cliente puede reintentar solo los items con {@code error} (RN-52).
 */
@Service
public class SyncAttendanceService implements SyncAttendanceUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncAttendanceService.class);

    private final RegisterAttendanceUseCase attendance;

    public SyncAttendanceService(RegisterAttendanceUseCase attendance) {
        this.attendance = attendance;
    }

    @Override
    public List<SyncItemResult> sync(UUID tenantId, UUID userId, List<RegisterAttendanceCommand> commands) {
        List<SyncItemResult> results = new ArrayList<>(commands.size());
        for (RegisterAttendanceCommand cmd : commands) {
            try {
                AttendanceResult r = attendance.register(tenantId, userId, cmd);  // transacción propia por item
                results.add(new SyncItemResult(cmd.operationUuid(), r.status(), r.rejectionReason(),
                        r.serverTime(), r.distanceToSiteM(), r.flags(), null));
            } catch (DomainException e) {
                results.add(errorItem(cmd.operationUuid(), e.getCode()));
            } catch (RuntimeException e) {
                log.warn("Fallo al sincronizar operación {}: {}", cmd.operationUuid(), e.toString());
                results.add(errorItem(cmd.operationUuid(), "SYNC_ERROR"));
            }
        }
        return results;
    }

    private SyncItemResult errorItem(UUID operationUuid, String error) {
        return new SyncItemResult(operationUuid, "ERROR", null, null, null, List.of(), error);
    }
}
