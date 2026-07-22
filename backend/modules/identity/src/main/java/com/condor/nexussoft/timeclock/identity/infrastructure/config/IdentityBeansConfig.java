package com.condor.nexussoft.timeclock.identity.infrastructure.config;

import com.condor.nexussoft.timeclock.identity.application.AuthPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;

/** Beans de soporte del módulo identity (política, reloj de servidor, encoder de contraseñas). */
@Configuration
public class IdentityBeansConfig {

    @Bean
    public Clock serverClock() {
        return Clock.systemUTC();  // hora de servidor autoritativa (ADR-003)
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthPolicy authPolicy(
            @Value("${security.auth.max-failed-logins:5}") int maxFailedLogins,
            @Value("${security.auth.lock-minutes:15}") long lockMinutes,
            @Value("${security.jwt.refresh-ttl-days:30}") long refreshTtlDays) {
        return new AuthPolicy(maxFailedLogins,
                Duration.ofMinutes(lockMinutes),
                Duration.ofDays(refreshTtlDays));
    }
}
