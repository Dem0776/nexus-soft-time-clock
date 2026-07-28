package com.condor.nexussoft.timeclock.hr.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Perfil de empleado: datos personales OPCIONALES (1-a-1 con un usuario).
 * Autocontenido (incluye teléfono) para no acoplarse al módulo identity.
 */
public record EmployeeProfile(
        UUID userId,
        UUID tenantId,
        LocalDate birthDate,
        LocalDate hireDate,
        Gender gender,
        String phone,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {}
