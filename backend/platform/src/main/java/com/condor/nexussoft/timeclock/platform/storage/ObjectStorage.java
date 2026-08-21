package com.condor.nexussoft.timeclock.platform.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Almacenamiento de objetos S3-compatible (MinIO, ADR-008). Es una abstracción <b>técnica</b>
 * de plataforma, no un puerto de dominio: cada bounded context define su propio puerto
 * (p. ej. {@code EvidenceStoragePort}) y lo implementa apoyándose en esta interfaz.
 *
 * <p>El acceso al contenido siempre es por <b>URL prefirmada</b> de vida corta; el backend
 * nunca transporta los binarios.
 */
public interface ObjectStorage {

    /** Bucket configurado. Es el único válido: la referencia que envíe un cliente se ignora. */
    String bucket();

    /** URL para que el cliente suba el objeto directamente (HTTP PUT) durante {@code ttl}. */
    PresignedUrl presignPut(String key, Duration ttl);

    /** URL de lectura de vida corta para mostrar el objeto (HTTP GET). */
    PresignedUrl presignGet(String key, Duration ttl);

    /**
     * Metadatos del objeto, o vacío si no existe. Es la comprobación autoritativa de que
     * una subida realmente ocurrió (una URL emitida no prueba que el PUT se completara).
     */
    Optional<ObjectMetadata> stat(String key);

    void delete(String key);

    /** Claves bajo un prefijo. Se usa para recolectar evidencias huérfanas. */
    List<String> listKeys(String prefix);

    /** Indica si el almacenamiento está operativo; {@code false} hace fallar cerrado a los llamadores. */
    boolean isAvailable();

    record PresignedUrl(String url, Instant expiresAt) {
    }

    record ObjectMetadata(long sizeBytes, String contentType, Instant lastModified, String etag) {
    }
}
