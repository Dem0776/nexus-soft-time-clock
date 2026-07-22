package com.condor.nexussoft.timeclock.attendance.application;

import com.condor.nexussoft.timeclock.attendance.domain.port.in.AttendanceResult;
import com.condor.nexussoft.timeclock.attendance.domain.port.in.RegisterAttendanceCommand;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAttendanceServiceTest {

    @Mock AttendanceRepositoryPort attendance;
    @Mock IdempotencyStorePort idempotency;
    @Mock NonceGuardPort nonceGuard;
    @Mock QrValidationPort qrValidation;
    @Mock GeofenceCheckPort geofenceCheck;
    @Mock FraudCheckPort fraudCheck;
    @Mock AttendanceEventPublisherPort events;

    RegisterAttendanceService service;

    final UUID tenantId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final UUID siteId = UUID.randomUUID();
    final Clock clock = Clock.fixed(Instant.parse("2026-07-21T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new RegisterAttendanceService(attendance, idempotency, nonceGuard, qrValidation,
                geofenceCheck, fraudCheck, events, clock);
    }

    private RegisterAttendanceCommand cmd() {
        return new RegisterAttendanceCommand(UUID.randomUUID(), siteId, "qr", 19.4326, -99.1332, 10.0,
                "ENTRADA", "dev-1", null, "ONLINE", false, false, false, false, true, false,
                null, null, null);
    }

    private void happyPathStubs() {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of(), false, null));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, true, 12.0, 50.0));
        when(nonceGuard.tryConsume(eq(tenantId), eq(siteId), eq("nonce-1"), any())).thenReturn(true);
    }

    @Test
    void registro_valido_esAceptado_yPublicaEvento() {
        happyPathStubs();

        AttendanceResult result = service.register(tenantId, userId, cmd());

        assertThat(result.status()).isEqualTo("ACCEPTED");
        assertThat(result.rejectionReason()).isNull();
        assertThat(result.serverTime()).isEqualTo(Instant.parse("2026-07-21T10:00:00Z"));  // hora de servidor
        verify(attendance).save(any());
        verify(idempotency).save(eq(tenantId), any(), any());
        verify(events).publish(any());
    }

    @Test
    void fueraDeGeocerca_esRechazado() {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of(), false, null));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, false, 350.0, 50.0));  // fuera del radio

        AttendanceResult result = service.register(tenantId, userId, cmd());

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("OUT_OF_GEOFENCE");
        verify(nonceGuard, never()).tryConsume(any(), any(), any(), any());  // no se consume nonce si ya rechazado
    }

    @Test
    void reenvio_delMismoUuid_devuelveResultadoPrevio_sinReprocesar() {
        AttendanceResult previous = new AttendanceResult(UUID.randomUUID(), "ACCEPTED", null,
                Instant.parse("2026-07-21T09:00:00Z"), 5.0, List.of());
        RegisterAttendanceCommand command = cmd();
        when(idempotency.find(tenantId, command.operationUuid())).thenReturn(Optional.of(previous));

        AttendanceResult result = service.register(tenantId, userId, command);

        assertThat(result).isEqualTo(previous);
        verify(attendance, never()).save(any());
        verify(qrValidation, never()).verify(any());
    }

    @Test
    void nonceReutilizado_esReplay() {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of(), false, null));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, true, 12.0, 50.0));
        when(nonceGuard.tryConsume(eq(tenantId), eq(siteId), eq("nonce-1"), any())).thenReturn(false);

        AttendanceResult result = service.register(tenantId, userId, cmd());

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("REPLAY_DETECTED");
    }

    @Test
    void mockLocation_bloqueante_esRechazadoPorFraude() {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of("MOCK_LOCATION"), true, "FRAUD_MOCK_LOCATION"));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, true, 12.0, 50.0));

        AttendanceResult result = service.register(tenantId, userId, cmd());

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("FRAUD_MOCK_LOCATION");
        assertThat(result.flags()).contains("MOCK_LOCATION");
    }
}
