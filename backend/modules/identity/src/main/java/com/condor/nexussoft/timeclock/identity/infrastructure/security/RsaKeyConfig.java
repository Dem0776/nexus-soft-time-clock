package com.condor.nexussoft.timeclock.identity.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Claves de firma del access token (RS256) y beans de codificación/decodificación JWT.
 * <p>
 * Si se configuran {@code security.jwt.private-key} y {@code security.jwt.public-key} (PEM,
 * normalmente por variable de entorno) se usa esa llave persistente, de modo que los tokens
 * sobreviven a reinicios/redeploys y son válidos entre instancias. Si no se configuran, se
 * genera un par RSA en memoria al arrancar (solo desarrollo: los tokens se invalidan al
 * reiniciar y difieren entre instancias). Ver ADR-007.
 */
@Configuration
public class RsaKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyConfig.class);

    @Bean
    public RSAKey rsaKey(
            @Value("${security.jwt.private-key:}") String privateKeyPem,
            @Value("${security.jwt.public-key:}") String publicKeyPem,
            @Value("${security.jwt.key-id:nexus-rsa}") String keyId) {
        if (StringUtils.hasText(privateKeyPem) && StringUtils.hasText(publicKeyPem)) {
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(toStream(privateKeyPem));
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(toStream(publicKeyPem));
            log.info("JWT firmado con llave RSA persistente (keyId={})", keyId);
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyId)
                    .build();
        }

        log.warn("security.jwt.private-key/public-key no configuradas: se genera un par RSA "
                + "en memoria (modo desarrollo). Los tokens se invalidarán al reiniciar. "
                + "Configura la llave por variable de entorno en producción (ADR-007).");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                    .privateKey((RSAPrivateKey) pair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar la llave RSA", e);
        }
    }

    /**
     * Convierte un PEM (posiblemente con saltos de línea escapados {@code \n}, habitual al
     * inyectarlo por variable de entorno) en un stream apto para {@link RsaKeyConverters}.
     */
    private static ByteArrayInputStream toStream(String pem) {
        String normalized = pem.replace("\\n", "\n");
        return new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }
}
