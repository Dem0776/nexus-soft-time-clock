package com.condor.nexussoft.timeclock.identity.domain.port.out;

/** Access token emitido junto con su vigencia en segundos. */
public record IssuedToken(String value, long expiresInSeconds) {
}
