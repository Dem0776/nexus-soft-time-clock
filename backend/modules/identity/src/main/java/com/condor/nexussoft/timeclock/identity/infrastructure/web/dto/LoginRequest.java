package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Petición de login. {@code companyCode} es opcional (se puede resolver por dominio de email). */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String companyCode) {
}
