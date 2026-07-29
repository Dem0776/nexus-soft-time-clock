package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta del administrador inicial (COMPANY_ADMIN) de una empresa, ejecutada por el
 * SUPER_ADMIN de plataforma. La contraseña es temporal y la fija el operador.
 */
public record ProvisionCompanyAdminRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        @Size(max = 60) String employeeCode,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
