package com.condor.nexussoft.timeclock.identity.domain.port.in;

/**
 * Comando de inicio de sesión. {@code companyCode} puede ser el código de empresa
 * o el dominio del email; si es null, se intenta resolver por el dominio del email.
 */
public record LoginCommand(String companyCode, String email, String password,
                           String ip, String userAgent) {
}
