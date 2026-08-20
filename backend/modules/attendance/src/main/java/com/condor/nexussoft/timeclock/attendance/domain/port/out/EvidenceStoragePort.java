package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * Almacenamiento de la evidencia fotográfica (RF-18, HU-13 CA2). El binario nunca pasa por el
 * backend: el cliente sube directo al object storage con una URL prefirmada (ADR-008).
 *
 * <p>La clave del objeto la <b>decide el servidor</b> al emitir el ticket de subida. El cliente
 * solo la devuelve al registrar, y el servidor la verifica: sin esa verificación bastaría con
 * inventar una clave para satisfacer la exigencia de foto del centro.
 */
public interface EvidenceStoragePort {

    /** Bucket configurado. Se persiste este valor, nunca el que declare el cliente. */
    String bucket();

    /** Tamaño máximo admitido para una evidencia. */
    long maxBytes();

    /** Tipos MIME admitidos para una evidencia. */
    boolean isContentTypeAllowed(String contentType);

    /** Emite la clave y la URL de subida para una evidencia de este usuario en este centro. */
    UploadTicket presignUpload(UUID tenantId, UUID userId, UUID workSiteId, Instant now);

    /** URL de lectura de vida corta para mostrar la evidencia en el portal. */
    String presignDownload(String objectKey);

    /**
     * Verifica que la clave corresponde a un objeto <b>realmente subido</b> por este usuario,
     * para este centro, dentro de la ventana de frescura y con tamaño/tipo admisibles.
     *
     * <p>El {@code sha256} que declara el cliente no se contrasta aquí: el ETag de S3 es un MD5
     * (y ni siquiera eso en subidas multiparte), así que comprobarlo exigiría descargar el objeto.
     * Se persiste como metadato declarado, útil para detectar manipulación posterior del binario.
     */
    Outcome validate(UUID tenantId, UUID userId, UUID workSiteId, String objectKey, Instant now);

    /**
     * @param bucket     bucket destino.
     * @param objectKey  clave emitida por el servidor.
     * @param uploadUrl  URL prefirmada para el PUT.
     * @param expiresAt  caducidad de {@code uploadUrl}.
     * @param maxBytes   tamaño máximo admitido del binario.
     */
    record UploadTicket(String bucket, String objectKey, String uploadUrl, Instant expiresAt, long maxBytes) {
    }

    /** Resultado de la verificación. Solo {@link #VALID} permite asociar la evidencia al registro. */
    enum Outcome {
        /** El objeto existe y cumple todas las restricciones. */
        VALID,
        /** No hay ningún objeto con esa clave: la subida nunca ocurrió o la clave es inventada. */
        MISSING,
        /** La clave no cuelga del prefijo de este tenant/usuario/centro. */
        FOREIGN_PREFIX,
        TOO_LARGE,
        BAD_CONTENT_TYPE,
        /** El objeto se subió hace demasiado tiempo: se está reutilizando una foto antigua. */
        STALE,
        /** El almacenamiento no respondió. Se trata como evidencia ausente (falla cerrado). */
        UNAVAILABLE
    }
}
