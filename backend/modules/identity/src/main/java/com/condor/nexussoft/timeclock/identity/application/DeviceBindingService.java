package com.condor.nexussoft.timeclock.identity.application;

import com.condor.nexussoft.timeclock.identity.domain.model.Device;
import com.condor.nexussoft.timeclock.identity.domain.model.DeviceAction;
import com.condor.nexussoft.timeclock.identity.domain.model.DeviceStatus;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceBindingPolicyPort;
import com.condor.nexussoft.timeclock.identity.domain.port.out.DeviceRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reconocimiento y administración de dispositivos (RF-28, RN-27). El enrolamiento es TOFU:
 * el primer dispositivo de un colaborador se confía en su primer uso; los adicionales quedan
 * {@code PENDING} y no registran asistencia hasta que un admin los apruebe (o se rechazan/
 * marcan según la política del tenant).
 */
@Service
public class DeviceBindingService implements DeviceBindingUseCase {

    private static final Set<String> VALID_PLATFORMS = Set.of("ANDROID", "IOS", "WEB");

    private final DeviceRepositoryPort devices;
    private final DeviceBindingPolicyPort policyPort;
    private final Clock clock;

    public DeviceBindingService(DeviceRepositoryPort devices, DeviceBindingPolicyPort policyPort, Clock clock) {
        this.devices = devices;
        this.policyPort = policyPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public DeviceDecision resolve(UUID tenantId, UUID userId, String deviceIdentifier, DeviceInfo info) {
        DeviceBindingPolicyPort.DeviceBindingPolicy policy = policyPort.find(tenantId);

        // Binding deshabilitado para el tenant: no se enrola ni se restringe (RN-27 configurable).
        if (!policy.enabled()) {
            return new DeviceDecision(true, null, DeviceAction.ALLOW);
        }

        // Sin identificador estable no hay dispositivo que reconocer: aplica la política de "no reconocido".
        if (deviceIdentifier == null || deviceIdentifier.isBlank()) {
            return new DeviceDecision(false, null, policy.action());
        }

        Instant now = clock.instant();
        String identifier = deviceIdentifier.trim();

        var existing = devices.findByIdentifier(tenantId, userId, identifier);
        if (existing.isPresent()) {
            Device device = existing.get();
            if (device.status() == DeviceStatus.BLOCKED) {
                return new DeviceDecision(false, DeviceStatus.BLOCKED, policy.action());
            }
            device.touch(now);
            devices.save(device);
            return device.isRecognized()
                    ? new DeviceDecision(true, DeviceStatus.TRUSTED, DeviceAction.ALLOW)
                    : new DeviceDecision(false, DeviceStatus.PENDING, policy.action());
        }

        // Dispositivo nunca visto: TOFU. Primer dispositivo del colaborador → confiado; el resto → pendiente.
        boolean firstDevice = !devices.existsForUser(tenantId, userId);
        String platform = normalizePlatform(info == null ? null : info.platform());
        String model = info == null ? null : info.model();
        String osVersion = info == null ? null : info.osVersion();

        Device device = firstDevice
                ? Device.enrollTrusted(tenantId, userId, identifier, platform, model, osVersion, now)
                : Device.enrollPending(tenantId, userId, identifier, platform, model, osVersion, now);
        devices.save(device);

        return firstDevice
                ? new DeviceDecision(true, DeviceStatus.TRUSTED, DeviceAction.ALLOW)
                : new DeviceDecision(false, DeviceStatus.PENDING, policy.action());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceView> list(UUID tenantId, UUID userId) {
        return devices.listByUser(tenantId, userId).stream().map(DeviceBindingService::toView).toList();
    }

    @Override
    @Transactional
    public DeviceView approve(UUID tenantId, UUID deviceId) {
        Device device = require(tenantId, deviceId);
        device.approve(clock.instant());
        return toView(devices.save(device));
    }

    @Override
    @Transactional
    public DeviceView revoke(UUID tenantId, UUID deviceId) {
        Device device = require(tenantId, deviceId);
        device.revoke(clock.instant());
        return toView(devices.save(device));
    }

    private Device require(UUID tenantId, UUID deviceId) {
        return devices.findById(tenantId, deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo", deviceId));
    }

    private static String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "ANDROID";
        }
        String p = platform.trim().toUpperCase();
        return VALID_PLATFORMS.contains(p) ? p : "ANDROID";
    }

    private static DeviceView toView(Device d) {
        return new DeviceView(d.id(), d.userId(), d.deviceIdentifier(), d.platform(), d.model(),
                d.osVersion(), d.status(), d.trusted(), d.lastSeenAt(), d.createdAt());
    }
}
