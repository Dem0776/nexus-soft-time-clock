package com.condor.nexussoft.timeclock.platform.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuración del almacenamiento de evidencias (ADR-008). Todo valor se inyecta por
 * variable de entorno (12-factor); ver {@code application.yml} bajo {@code storage.minio}.
 *
 * @param enabled               si es false, el backend arranca sin storage y las políticas que exigen
 *                              evidencia <b>fallan cerrado</b> (rechazan el registro).
 * @param endpoint              endpoint interno (red de Docker) para stat/delete/list.
 * @param publicEndpoint        origen que verá el cliente. SigV4 firma el {@code Host}, así que las URLs
 *                              prefirmadas deben generarse contra este valor y no contra el interno.
 * @param bucket                bucket único de evidencias. Coincide con el prefijo de ruta expuesto por
 *                              NGINX ({@code /evidence/}) para que el path canónico de la firma cuadre.
 * @param putTtlSeconds         vigencia de la URL de subida.
 * @param getTtlSeconds         vigencia de la URL de lectura.
 * @param maxBytes              tamaño máximo aceptado del objeto.
 * @param allowedContentTypes   tipos MIME admitidos.
 * @param uploadFreshnessSeconds antigüedad máxima de la subida al registrar: corta la reutilización de
 *                              fotos antiguas. Mide el momento de la SUBIDA, no el de la captura, por lo
 *                              que no penaliza a los registros offline (que suben al recuperar la red).
 * @param sse                   {@code SSE_S3} activa el cifrado en reposo del bucket; {@code NONE} lo omite.
 * @param retentionDays         expiración de los objetos (ciclo de vida del bucket).
 */
@ConfigurationProperties(prefix = "storage.minio")
public record StorageProperties(
        boolean enabled,
        String endpoint,
        String publicEndpoint,
        String accessKey,
        String secretKey,
        String bucket,
        String region,
        int putTtlSeconds,
        int getTtlSeconds,
        long maxBytes,
        List<String> allowedContentTypes,
        long uploadFreshnessSeconds,
        Sse sse,
        int retentionDays) {

    public enum Sse {
        NONE, SSE_S3
    }

    /** Endpoint de firma: el público si está definido, con el interno como respaldo (dev). */
    public String signingEndpoint() {
        return publicEndpoint == null || publicEndpoint.isBlank() ? endpoint : publicEndpoint;
    }

    public boolean isContentTypeAllowed(String contentType) {
        return contentType != null && allowedContentTypes != null
                && allowedContentTypes.stream().anyMatch(t -> t.equalsIgnoreCase(contentType));
    }
}
