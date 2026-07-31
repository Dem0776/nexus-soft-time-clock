package com.condor.nexussoft.timeclock.identity.domain.model;

/** Acción que debe tomar el registro de asistencia ante el reconocimiento del dispositivo (RN-27). */
public enum DeviceAction {
    /** Dispositivo reconocido (o binding deshabilitado): no restringe. */
    ALLOW,
    /** Dispositivo no reconocido: aceptar el registro pero marcarlo con {@code UNTRUSTED_DEVICE}. */
    FLAG,
    /** Dispositivo no reconocido: rechazar el registro con {@code UNTRUSTED_DEVICE}. */
    REJECT
}
