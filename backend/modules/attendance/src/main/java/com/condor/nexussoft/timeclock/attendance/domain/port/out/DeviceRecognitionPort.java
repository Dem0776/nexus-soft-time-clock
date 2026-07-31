package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.util.UUID;

/**
 * Puente hacia Identity para el device binding (RF-28, RN-27): reconoce/enrola el dispositivo del
 * colaborador y devuelve la acción que debe aplicar el registro de asistencia.
 */
public interface DeviceRecognitionPort {

    /** Acción resultante ante el dispositivo, ya resuelta contra la política del tenant. */
    enum Action { ALLOW, FLAG, REJECT }

    record DeviceRecognition(boolean recognized, Action action) {}

    DeviceRecognition resolve(UUID tenantId, UUID userId, String deviceIdentifier,
                              String platform, String model, String osVersion);
}
