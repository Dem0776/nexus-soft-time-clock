package com.condor.nexussoft.timeclock.attendance.application;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceEventType;
import com.condor.nexussoft.timeclock.attendance.domain.AttendanceSequenceValidator.LastEvent;
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
    @Mock WorkSitePolicyPort sitePolicy;
    @Mock SchedulePolicyPort schedulePolicy;
    @Mock EventTypeConfigPort eventTypeConfig;
    @Mock AttendanceEventPublisherPort events;

    RegisterAttendanceService service;

    final UUID tenantId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final UUID siteId = UUID.randomUUID();
    final Clock clock = Clock.fixed(Instant.parse("2026-07-21T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new RegisterAttendanceService(attendance, idempotency, nonceGuard, qrValidation,
                geofenceCheck, fraudCheck, sitePolicy, schedulePolicy, eventTypeConfig, events, clock);
    }

    /** Sin overrides de tipos de evento → todos los intermedios habilitados. */
    private void allEventTypesEnabledStub() {
        when(eventTypeConfig.findByTenant(tenantId)).thenReturn(java.util.Map.of());
    }

    /** Política de centro sin exigencias (foto/biometría opcionales, umbral por default). */
    private void permissiveSiteStub() {
        when(sitePolicy.find(tenantId, siteId)).thenReturn(WorkSitePolicyPort.SitePolicy.permissive());
    }

    /** El colaborador no tiene turno asignado vigente → sin restricción horaria. */
    private void noScheduleStub() {
        when(schedulePolicy.check(eq(tenantId), eq(userId), eq(siteId), any()))
                .thenReturn(SchedulePolicyPort.ScheduleCheck.NO_SCHEDULE);
    }

    private RegisterAttendanceCommand cmd() {
        return cmd("ENTRADA");
    }

    private RegisterAttendanceCommand cmd(String eventType) {
        return new RegisterAttendanceCommand(UUID.randomUUID(), siteId, "qr", 19.4326, -99.1332, 10.0,
                eventType, "dev-1", null, "ONLINE", false, false, false, false, true, false,
                null, null, null);
    }

    /** Deja pasar QR + antifraude + geocerca para llegar a la validación de secuencia. */
    private void validationsUpToSequenceStubs() {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of(), false, null));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, true, 12.0, 50.0));
        permissiveSiteStub();
        noScheduleStub();
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
        permissiveSiteStub();
        noScheduleStub();
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
        permissiveSiteStub();

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
        permissiveSiteStub();
        noScheduleStub();

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
        permissiveSiteStub();

        AttendanceResult result = service.register(tenantId, userId, cmd());

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("FRAUD_MOCK_LOCATION");
        assertThat(result.flags()).contains("MOCK_LOCATION");
    }

    @Test
    void salida_sinEntradaAbierta_esRechazadaPorSecuencia() {
        validationsUpToSequenceStubs();
        when(attendance.findLastAcceptedEvent(tenantId, userId)).thenReturn(Optional.empty());

        AttendanceResult result = service.register(tenantId, userId, cmd("SALIDA"));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("INVALID_SEQUENCE");
        verify(nonceGuard, never()).tryConsume(any(), any(), any(), any());
    }

    @Test
    void salida_conEntradaAbierta_mismoCentro_esAceptada() {
        validationsUpToSequenceStubs();
        when(attendance.findLastAcceptedEvent(tenantId, userId))
                .thenReturn(Optional.of(new LastEvent(AttendanceEventType.ENTRADA, siteId)));
        when(nonceGuard.tryConsume(eq(tenantId), eq(siteId), eq("nonce-1"), any())).thenReturn(true);

        AttendanceResult result = service.register(tenantId, userId, cmd("SALIDA"));

        assertThat(result.status()).isEqualTo("ACCEPTED");
        assertThat(result.rejectionReason()).isNull();
    }

    @Test
    void dobleInicioDescanso_esRechazadoPorSecuencia() {
        validationsUpToSequenceStubs();
        allEventTypesEnabledStub();
        when(attendance.findLastAcceptedEvent(tenantId, userId))
                .thenReturn(Optional.of(new LastEvent(AttendanceEventType.INICIO_DESCANSO, siteId)));

        AttendanceResult result = service.register(tenantId, userId, cmd("INICIO_DESCANSO"));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("INVALID_SEQUENCE");
    }

    /** Como validationsUpToSequenceStubs pero permite fijar la política del centro. */
    private void baseStubsWithPolicy(WorkSitePolicyPort.SitePolicy policy) {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of(), false, null));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, true, 12.0, 50.0));
        when(sitePolicy.find(tenantId, siteId)).thenReturn(policy);
    }

    @Test
    void umbralDePrecisionPorCentro_masEstricto_rechazaLOW_GPS_ACCURACY() {
        // precisión del dispositivo 10 m: pasa el default (50) pero no el umbral por-centro (5).
        baseStubsWithPolicy(new WorkSitePolicyPort.SitePolicy(5, false, false));

        AttendanceResult result = service.register(tenantId, userId, cmd("ENTRADA"));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("LOW_GPS_ACCURACY");
        verify(nonceGuard, never()).tryConsume(any(), any(), any(), any());
    }

    @Test
    void fotoObligatoria_sinEvidencia_esRechazada() {
        baseStubsWithPolicy(new WorkSitePolicyPort.SitePolicy(null, true, false));
        noScheduleStub();
        when(attendance.findLastAcceptedEvent(tenantId, userId)).thenReturn(Optional.empty());

        AttendanceResult result = service.register(tenantId, userId, cmd("ENTRADA"));  // sin evidenceKey

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("PHOTO_REQUIRED");
        verify(nonceGuard, never()).tryConsume(any(), any(), any(), any());
    }

    @Test
    void biometriaObligatoria_sinVerificacion_esRechazada() {
        baseStubsWithPolicy(new WorkSitePolicyPort.SitePolicy(null, false, true));
        noScheduleStub();
        when(attendance.findLastAcceptedEvent(tenantId, userId)).thenReturn(Optional.empty());

        AttendanceResult result = service.register(tenantId, userId, cmd("ENTRADA"));  // biometricVerified=false

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("BIOMETRIC_REQUIRED");
    }

    @Test
    void tipoDeEventoDeshabilitado_esRechazado() {
        when(idempotency.find(eq(tenantId), any())).thenReturn(Optional.empty());
        when(qrValidation.verify("qr"))
                .thenReturn(new QrValidationPort.QrCheck(true, false, tenantId, siteId, "nonce-1"));
        when(fraudCheck.evaluate(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new FraudCheckPort.FraudCheckResult(List.of(), false, null));
        when(geofenceCheck.check(eq(tenantId), eq(siteId), anyDouble(), anyDouble()))
                .thenReturn(new GeofenceCheckPort.GeofenceCheck(true, true, 12.0, 50.0));
        permissiveSiteStub();
        // La empresa deshabilitó CAMBIO_SITIO.
        when(eventTypeConfig.findByTenant(tenantId)).thenReturn(java.util.Map.of(
                AttendanceEventType.CAMBIO_SITIO,
                new com.condor.nexussoft.timeclock.attendance.domain.EventTypeSetting(
                        AttendanceEventType.CAMBIO_SITIO, false, "Cambio de sitio")));

        AttendanceResult result = service.register(tenantId, userId, cmd("CAMBIO_SITIO"));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("EVENT_TYPE_DISABLED");
        verify(nonceGuard, never()).tryConsume(any(), any(), any(), any());
    }

    @Test
    void fueraDeVentanaDeTurno_esRechazada() {
        baseStubsWithPolicy(WorkSitePolicyPort.SitePolicy.permissive());
        when(schedulePolicy.check(eq(tenantId), eq(userId), eq(siteId), any()))
                .thenReturn(SchedulePolicyPort.ScheduleCheck.OUT_OF_WINDOW);

        AttendanceResult result = service.register(tenantId, userId, cmd("ENTRADA"));

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("OUT_OF_SCHEDULE");
        verify(nonceGuard, never()).tryConsume(any(), any(), any(), any());
    }
}
