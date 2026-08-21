package com.condor.nexussoft.timeclock.platform.storage;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Almacenamiento inerte, activo cuando {@code storage.minio.enabled=false} (entornos sin MinIO
 * y pruebas que no necesitan levantarlo). Permite que el contexto arranque sin dejar a los
 * consumidores de {@link ObjectStorage} sin bean que inyectar.
 *
 * <p>Falla <b>cerrado</b> a propósito: {@link #stat} nunca encuentra el objeto, así que la
 * verificación de una evidencia da «no existe» y un centro que exija foto rechazará el registro.
 * Lo contrario —dar por buena una evidencia que no se puede comprobar— vaciaría de sentido la
 * exigencia. Firmar URLs sí lanza, porque no hay forma sensata de simularlo.
 */
public class DisabledObjectStorage implements ObjectStorage {

    private final StorageProperties props;

    public DisabledObjectStorage(StorageProperties props) {
        this.props = props;
    }

    @Override
    public String bucket() {
        return props.bucket();
    }

    @Override
    public PresignedUrl presignPut(String key, Duration ttl) {
        throw new IllegalStateException("El almacenamiento de evidencias está desactivado.");
    }

    @Override
    public PresignedUrl presignGet(String key, Duration ttl) {
        throw new IllegalStateException("El almacenamiento de evidencias está desactivado.");
    }

    @Override
    public Optional<ObjectMetadata> stat(String key) {
        return Optional.empty();
    }

    @Override
    public void delete(String key) {
        // Sin almacenamiento no hay nada que borrar; el recolector de huérfanos no debe fallar.
    }

    @Override
    public List<String> listKeys(String prefix) {
        return List.of();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
