package com.condor.nexussoft.timeclock.identity.domain.port.out;

import com.condor.nexussoft.timeclock.identity.domain.model.DeviceAction;

import java.util.UUID;

/** Política de device binding por tenant (columnas {@code device_binding_*} de {@code company_settings}). */
public interface DeviceBindingPolicyPort {

    /**
     * @param enabled si la validación de dispositivo está activa (RN-27).
     * @param action  acción ante dispositivo no reconocido: {@link DeviceAction#REJECT} o {@link DeviceAction#FLAG}.
     */
    record DeviceBindingPolicy(boolean enabled, DeviceAction action) {

        /** Valor por defecto de plataforma cuando el tenant no tiene fila de settings. */
        public static DeviceBindingPolicy defaults() {
            return new DeviceBindingPolicy(true, DeviceAction.REJECT);
        }
    }

    DeviceBindingPolicy find(UUID tenantId);
}
