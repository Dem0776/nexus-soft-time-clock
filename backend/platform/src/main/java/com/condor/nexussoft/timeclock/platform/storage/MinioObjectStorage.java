package com.condor.nexussoft.timeclock.platform.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Adaptador de {@link ObjectStorage} sobre el SDK de MinIO. */
public class MinioObjectStorage implements ObjectStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStorage.class);

    private final MinioClient internal;
    private final MinioClient signer;
    private final StorageProperties props;

    public MinioObjectStorage(MinioClient internal, MinioClient signer, StorageProperties props) {
        this.internal = internal;
        this.signer = signer;
        this.props = props;
    }

    @Override
    public String bucket() {
        return props.bucket();
    }

    @Override
    public PresignedUrl presignPut(String key, Duration ttl) {
        // Una URL firmada por query-string no puede fijar Content-Length ni Content-Type sin obligar
        // al cliente a reproducirlos byte a byte en la firma (frágil). El tamaño lo acota NGINX
        // (client_max_body_size) y tanto tamaño como tipo se verifican con stat() al registrar.
        return presign(Method.PUT, key, ttl);
    }

    @Override
    public PresignedUrl presignGet(String key, Duration ttl) {
        return presign(Method.GET, key, ttl);
    }

    private PresignedUrl presign(Method method, String key, Duration ttl) {
        try {
            String url = signer.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(props.bucket())
                    .object(key)
                    .expiry((int) ttl.toSeconds(), TimeUnit.SECONDS)
                    .build());
            return new PresignedUrl(url, Instant.now().plus(ttl));
        } catch (Exception e) {
            throw new ObjectStorageException("No se pudo generar la URL prefirmada de " + method, e);
        }
    }

    @Override
    public Optional<ObjectMetadata> stat(String key) {
        try {
            StatObjectResponse r = internal.statObject(StatObjectArgs.builder()
                    .bucket(props.bucket())
                    .object(key)
                    .build());
            return Optional.of(new ObjectMetadata(r.size(), r.contentType(),
                    r.lastModified() == null ? Instant.EPOCH : r.lastModified().toInstant(), r.etag()));
        } catch (ErrorResponseException e) {
            // Objeto (o bucket) inexistente: es un resultado normal, no un fallo de infraestructura.
            return Optional.empty();
        } catch (Exception e) {
            throw new ObjectStorageException("No se pudo consultar el objeto " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            internal.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.bucket())
                    .object(key)
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("No se pudo borrar el objeto " + key, e);
        }
    }

    @Override
    public List<String> listKeys(String prefix) {
        List<String> keys = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = internal.listObjects(ListObjectsArgs.builder()
                    .bucket(props.bucket())
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            for (Result<Item> r : results) {
                keys.add(r.get().objectName());
            }
            return keys;
        } catch (Exception e) {
            throw new ObjectStorageException("No se pudieron listar los objetos bajo " + prefix, e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            internal.bucketExists(io.minio.BucketExistsArgs.builder().bucket(props.bucket()).build());
            return true;
        } catch (Exception e) {
            log.warn("Almacenamiento de evidencias no disponible: {}", e.getMessage());
            return false;
        }
    }

    /** Fallo de infraestructura del almacenamiento (no confundir con "objeto inexistente"). */
    public static class ObjectStorageException extends RuntimeException {
        public ObjectStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
