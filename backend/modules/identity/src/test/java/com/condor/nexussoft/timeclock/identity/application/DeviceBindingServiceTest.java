package com.condor.nexussoft.timeclock.identity.application;

import com.condor.nexussoft.timeclock.identity.domain.model.Device;
import com.condor.nexussoft.timeclock.identity.domain.model.DeviceAction;
import com.condor.nexussoft.timeclock.identity.domain.model.DeviceStatus;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase.DeviceDecision;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase.DeviceInfo;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase.DeviceView;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceBindingPolicyPort;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceBindingPolicyPort.DeviceBindingPolicy;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceBindingServiceTest {

    @Mock DeviceRepositoryPort devices;
    @Mock DeviceBindingPolicyPort policyPort;

    DeviceBindingService service;

    final UUID tenantId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant now = Instant.parse("2026-07-21T10:00:00Z");
    final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    final DeviceInfo info = new DeviceInfo("ANDROID", "Pixel 7", "Android 14");

    @BeforeEach
    void setUp() {
        service = new DeviceBindingService(devices, policyPort, clock);
    }

    private void policy(boolean enabled, DeviceAction action) {
        when(policyPort.find(tenantId)).thenReturn(new DeviceBindingPolicy(enabled, action));
    }

    @Test
    void bindingDeshabilitado_permiteSinEnrolar() {
        policy(false, DeviceAction.REJECT);

        DeviceDecision decision = service.resolve(tenantId, userId, "dev-1", info);

        assertThat(decision.recognized()).isTrue();
        assertThat(decision.action()).isEqualTo(DeviceAction.ALLOW);
        verify(devices, never()).save(any());
    }

    @Test
    void primerDispositivo_seConfiaAutomaticamente_TOFU() {
        policy(true, DeviceAction.REJECT);
        when(devices.findByIdentifier(tenantId, userId, "dev-1")).thenReturn(Optional.empty());
        when(devices.existsForUser(tenantId, userId)).thenReturn(false);   // primer dispositivo

        DeviceDecision decision = service.resolve(tenantId, userId, "dev-1", info);

        assertThat(decision.recognized()).isTrue();
        assertThat(decision.status()).isEqualTo(DeviceStatus.TRUSTED);
        assertThat(decision.action()).isEqualTo(DeviceAction.ALLOW);

        ArgumentCaptor<Device> saved = ArgumentCaptor.forClass(Device.class);
        verify(devices).save(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo(DeviceStatus.TRUSTED);
        assertThat(saved.getValue().platform()).isEqualTo("ANDROID");
    }

    @Test
    void segundoDispositivoNuevo_quedaPendiente_yAplicaPolitica() {
        policy(true, DeviceAction.REJECT);
        when(devices.findByIdentifier(tenantId, userId, "dev-2")).thenReturn(Optional.empty());
        when(devices.existsForUser(tenantId, userId)).thenReturn(true);    // ya tiene otro dispositivo

        DeviceDecision decision = service.resolve(tenantId, userId, "dev-2", info);

        assertThat(decision.recognized()).isFalse();
        assertThat(decision.status()).isEqualTo(DeviceStatus.PENDING);
        assertThat(decision.action()).isEqualTo(DeviceAction.REJECT);

        ArgumentCaptor<Device> saved = ArgumentCaptor.forClass(Device.class);
        verify(devices).save(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo(DeviceStatus.PENDING);
    }

    @Test
    void dispositivoConfiado_esReconocido_yActualizaLastSeen() {
        policy(true, DeviceAction.REJECT);
        Device trusted = new Device(UUID.randomUUID(), tenantId, userId, "dev-1", "ANDROID",
                "Pixel 7", "Android 14", true, true, now.minusSeconds(3600), now.minusSeconds(7200));
        when(devices.findByIdentifier(tenantId, userId, "dev-1")).thenReturn(Optional.of(trusted));

        DeviceDecision decision = service.resolve(tenantId, userId, "dev-1", info);

        assertThat(decision.recognized()).isTrue();
        assertThat(decision.action()).isEqualTo(DeviceAction.ALLOW);
        verify(devices).save(argThat(d -> d.lastSeenAt().equals(now)));   // touch
    }

    @Test
    void dispositivoPendiente_noEsReconocido() {
        policy(true, DeviceAction.REJECT);
        Device pending = new Device(UUID.randomUUID(), tenantId, userId, "dev-2", "ANDROID",
                null, null, false, true, now.minusSeconds(60), now.minusSeconds(120));
        when(devices.findByIdentifier(tenantId, userId, "dev-2")).thenReturn(Optional.of(pending));

        DeviceDecision decision = service.resolve(tenantId, userId, "dev-2", info);

        assertThat(decision.recognized()).isFalse();
        assertThat(decision.status()).isEqualTo(DeviceStatus.PENDING);
        assertThat(decision.action()).isEqualTo(DeviceAction.REJECT);
    }

    @Test
    void dispositivoBloqueado_noEsReconocido_yNoSeReactiva() {
        policy(true, DeviceAction.FLAG);
        Device blocked = new Device(UUID.randomUUID(), tenantId, userId, "dev-3", "ANDROID",
                null, null, false, false, now.minusSeconds(60), now.minusSeconds(120));
        when(devices.findByIdentifier(tenantId, userId, "dev-3")).thenReturn(Optional.of(blocked));

        DeviceDecision decision = service.resolve(tenantId, userId, "dev-3", info);

        assertThat(decision.recognized()).isFalse();
        assertThat(decision.status()).isEqualTo(DeviceStatus.BLOCKED);
        assertThat(decision.action()).isEqualTo(DeviceAction.FLAG);
        verify(devices, never()).save(any());
    }

    @Test
    void sinIdentificador_conBindingActivo_aplicaPoliticaSinEnrolar() {
        policy(true, DeviceAction.REJECT);

        DeviceDecision decision = service.resolve(tenantId, userId, null, info);

        assertThat(decision.recognized()).isFalse();
        assertThat(decision.action()).isEqualTo(DeviceAction.REJECT);
        verify(devices, never()).save(any());
    }

    @Test
    void aprobar_confiaElDispositivo() {
        Device pending = new Device(UUID.randomUUID(), tenantId, userId, "dev-2", "ANDROID",
                null, null, false, true, now.minusSeconds(60), now.minusSeconds(120));
        when(devices.findById(tenantId, pending.id())).thenReturn(Optional.of(pending));
        when(devices.save(any())).thenAnswer(i -> i.getArgument(0));

        DeviceView view = service.approve(tenantId, pending.id());

        assertThat(view.status()).isEqualTo(DeviceStatus.TRUSTED);
        assertThat(view.trusted()).isTrue();
    }

    @Test
    void revocar_bloqueaElDispositivo() {
        Device trusted = new Device(UUID.randomUUID(), tenantId, userId, "dev-1", "ANDROID",
                null, null, true, true, now.minusSeconds(60), now.minusSeconds(120));
        when(devices.findById(tenantId, trusted.id())).thenReturn(Optional.of(trusted));
        when(devices.save(any())).thenAnswer(i -> i.getArgument(0));

        DeviceView view = service.revoke(tenantId, trusted.id());

        assertThat(view.status()).isEqualTo(DeviceStatus.BLOCKED);
        assertThat(view.trusted()).isFalse();
    }
}
