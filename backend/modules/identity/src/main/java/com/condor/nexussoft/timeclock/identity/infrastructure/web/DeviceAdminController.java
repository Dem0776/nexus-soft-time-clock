package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase;
import com.condor.nexussoft.timeclock.identity.infrastructure.web.dto.DeviceResponse;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Administración de dispositivos vinculados de los colaboradores (RF-28, RN-27).
 * Requiere {@code device:manage}. El enrolamiento en sí es automático (TOFU) durante el registro
 * de asistencia; aquí el administrador solo consulta, aprueba y revoca.
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('device:manage')")
public class DeviceAdminController {

    private final DeviceBindingUseCase devices;

    public DeviceAdminController(DeviceBindingUseCase devices) {
        this.devices = devices;
    }

    @GetMapping("/users/{userId}/devices")
    public List<DeviceResponse> list(@PathVariable UUID userId) {
        return devices.list(tenant(), userId).stream().map(DeviceResponse::from).toList();
    }

    @PostMapping("/devices/{deviceId}/approve")
    public DeviceResponse approve(@PathVariable UUID deviceId) {
        return DeviceResponse.from(devices.approve(tenant(), deviceId));
    }

    @PostMapping("/devices/{deviceId}/revoke")
    public DeviceResponse revoke(@PathVariable UUID deviceId) {
        return DeviceResponse.from(devices.revoke(tenant(), deviceId));
    }

    private UUID tenant() {
        return TenantContext.require();
    }
}
