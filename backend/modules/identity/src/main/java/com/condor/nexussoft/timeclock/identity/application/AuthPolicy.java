package com.condor.nexussoft.timeclock.identity.application;

import java.time.Duration;

/**
 * Política de autenticación (valores por defecto de plataforma; en el futuro pueden
 * derivarse de {@code company_settings} por tenant).
 */
public record AuthPolicy(int maxFailedLogins, Duration lockDuration, Duration refreshTtl) {

    public static AuthPolicy defaults() {
        return new AuthPolicy(5, Duration.ofMinutes(15), Duration.ofDays(30));
    }
}
