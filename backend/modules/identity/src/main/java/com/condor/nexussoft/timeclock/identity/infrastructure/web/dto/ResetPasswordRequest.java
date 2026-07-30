package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Restablecimiento de contraseña por un administrador (no requiere la contraseña actual). */
public record ResetPasswordRequest(
        @NotBlank @Size(min = 8, max = 100) String newPassword) {
}
