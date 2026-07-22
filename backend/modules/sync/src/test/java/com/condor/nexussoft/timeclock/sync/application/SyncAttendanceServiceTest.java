package com.condor.nexussoft.timeclock.sync.application;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceResult;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceCommand;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.sync.domain.port.in.SyncItemResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncAttendanceServiceTest {

    @Mock RegisterAttendanceUseCase attendance;

    final UUID tenantId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    private RegisterAttendanceCommand cmd() {
        return new RegisterAttendanceCommand(UUID.randomUUID(), UUID.randomUUID(), "qr", 19.0, -99.0, 10.0,
                "ENTRADA", null, null, "OFFLINE_SYNC", false, false, false, false, true, false, null, null, null);
    }

    @Test
    void lote_conUnItemFallido_aislaElError_yProcesaElResto() {
        SyncAttendanceService service = new SyncAttendanceService(attendance);
        RegisterAttendanceCommand ok = cmd();
        RegisterAttendanceCommand bad = cmd();

        when(attendance.register(tenantId, userId, ok)).thenReturn(new AttendanceResult(
                UUID.randomUUID(), "ACCEPTED", null, Instant.parse("2026-07-21T10:00:00Z"), 8.0, List.of()));
        when(attendance.register(tenantId, userId, bad))
                .thenThrow(new DomainException("OUT_OF_GEOFENCE", "fuera"));

        List<SyncItemResult> results = service.sync(tenantId, userId, List.of(ok, bad));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).status()).isEqualTo("ACCEPTED");
        assertThat(results.get(0).error()).isNull();
        assertThat(results.get(1).status()).isEqualTo("ERROR");
        assertThat(results.get(1).error()).isEqualTo("OUT_OF_GEOFENCE");
    }
}
