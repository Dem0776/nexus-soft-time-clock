package com.condor.nexussoft.timeclock.identity.domain.port.in;

/** Par de tokens emitido tras autenticar o renovar (ADR-007). */
public record AuthTokens(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static AuthTokens bearer(String accessToken, String refreshToken, long expiresIn) {
        return new AuthTokens(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
