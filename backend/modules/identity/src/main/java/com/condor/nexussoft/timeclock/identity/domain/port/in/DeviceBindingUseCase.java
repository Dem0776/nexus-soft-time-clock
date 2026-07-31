package com.condor.nexussoft.timeclock.identity.domain.port.in;

import com.condor.nexussoft.timeclock.identity.domain.model.DeviceAction;
import com.condor.nexussoft.timeclock.identity.domain.model.DeviceStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Device binding (RF-28, RN-27): reconoce/enrola dispositivos por colaborador (TOFU) durante el
 * registro de asistencia, y permite a un administrador listarlos, aprobarlos y revocarlos.
 */
public interface DeviceBindingUseCase {

    /** Metadatos del dispositivo reportados por el cliente al presentarse. */
    record DeviceInfo(String platform, String model, String osVersion) {}

    /**
     * Resultado del reconocimiento para el flujo de registro.
     * @param recognized el dispositivo es reconocido y confiado (o el binding está deshabilitado).
     * @param status     estado resultante del dispositivo ({@code null} si el binding está deshabilitado).
     * @param action     acción que debe aplicar el registro de asistencia (ALLOW/FLAG/REJECT).
     */
    record DeviceDecision(boolean recognized, DeviceStatus status, DeviceAction action) {}

    /** Proyección de lectura para administración. */
    record DeviceView(UUID id, UUID userId, String deviceIdentifier, String platform, String model,
                      String osVersion, DeviceStatus status, boolean trusted,
                      Instant lastSeenAt, Instant createdAt) {}

    /**
     * Reconoce el dispositivo del colaborador aplicando la política del tenant y el enrolamiento TOFU.
     * Actualiza {@code last_seen_at} y crea la fila si es la primera vez que se ve el identificador.
     */
    DeviceDecision resolve(UUID tenantId, UUID userId, String deviceIdentifier, DeviceInfo info);

    /** Lista los dispositivos de un colaborador (más reciente primero). */
    List<DeviceView> list(UUID tenantId, UUID userId);

    /** Confía un dispositivo (aprobación de administrador). */
    DeviceView approve(UUID tenantId, UUID deviceId);

    /** Revoca/bloquea un dispositivo (administrador). */
    DeviceView revoke(UUID tenantId, UUID deviceId);
}
