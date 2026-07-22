package com.condor.nexussoft.timeclock.identity.infrastructure.web.dto;

import com.condor.nexussoft.timeclock.identity.domain.port.in.AuthTokens;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static TokenResponse from(AuthTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.refreshToken(),
                tokens.tokenType(), tokens.expiresIn());
    }
}
