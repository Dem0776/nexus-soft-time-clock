package com.condor.nexussoft.timeclock.identity.infrastructure.security;

import com.condor.nexussoft.timeclock.identity.domain.port.out.OpaqueTokenServicePort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh tokens opacos: valor aleatorio de 256 bits (base64url) para el cliente y
 * hash SHA-256 (hex) para persistir/buscar. Nunca se almacena el valor en claro (ADR-007).
 */
@Component
public class Sha256OpaqueTokenService implements OpaqueTokenServicePort {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
