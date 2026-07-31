package com.condor.nexussoft.timeclock.identity.domain.model;

/**
 * Estado de reconocimiento de un dispositivo (RF-28, RN-27). Se deriva de las columnas
 * físicas {@code is_trusted}/{@code status} de la tabla {@code devices}:
 * <ul>
 *   <li>{@code TRUSTED}  — activo y confiado; puede registrar asistencia.</li>
 *   <li>{@code PENDING}  — activo pero aún no confiado (dispositivo nuevo bajo TOFU); espera aprobación.</li>
 *   <li>{@code BLOCKED}  — bloqueado/revocado por un administrador.</li>
 * </ul>
 */
public enum DeviceStatus {
    PENDING,
    TRUSTED,
    BLOCKED
}
