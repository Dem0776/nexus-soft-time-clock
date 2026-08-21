package com.condor.nexussoft.timeclock.platform.storage;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el almacenamiento de objetos. Se crean <b>dos</b> clientes deliberadamente:
 *
 * <ul>
 *   <li>{@code internal} apunta al endpoint de la red interna y ejecuta las operaciones reales
 *       (stat, delete, list, administración del bucket).</li>
 *   <li>{@code signer} apunta al endpoint público y solo calcula URLs prefirmadas — una operación
 *       local, sin red.</li>
 * </ul>
 *
 * El desdoblamiento es necesario porque SigV4 firma la cabecera {@code Host}: si la URL se generara
 * contra {@code http://minio:9000}, el cliente la usaría contra el host público y MinIO respondería
 * {@code SignatureDoesNotMatch}.
 *
 * <p>Los condicionales van <b>por bean</b>, no en la clase: siempre debe existir un
 * {@link ObjectStorage} que inyectar, porque quienes lo consumen no son opcionales. Con
 * {@code storage.minio.enabled=false} se registra el sustituto inerte en lugar de dejar el
 * contenedor sin el bean, que impediría arrancar. Se usa {@code @ConditionalOnProperty} en ambos
 * —y no {@code @ConditionalOnMissingBean}— porque este último depende del orden de registro.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    private static final String PREFIX = "storage.minio";
    private static final String FLAG = "enabled";

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = FLAG, havingValue = "true", matchIfMissing = true)
    public MinioClient minioInternalClient(StorageProperties props) {
        return MinioClient.builder()
                .endpoint(props.endpoint())
                .credentials(props.accessKey(), props.secretKey())
                .region(props.region())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = FLAG, havingValue = "true", matchIfMissing = true)
    public MinioClient minioSigningClient(StorageProperties props) {
        return MinioClient.builder()
                .endpoint(props.signingEndpoint())
                .credentials(props.accessKey(), props.secretKey())
                .region(props.region())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = FLAG, havingValue = "true", matchIfMissing = true)
    public ObjectStorage objectStorage(MinioClient minioInternalClient, MinioClient minioSigningClient,
                                       StorageProperties props) {
        return new MinioObjectStorage(minioInternalClient, minioSigningClient, props);
    }

    /** Sustituto inerte cuando el almacenamiento está desactivado (ver {@link DisabledObjectStorage}). */
    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = FLAG, havingValue = "false")
    public ObjectStorage disabledObjectStorage(StorageProperties props) {
        return new DisabledObjectStorage(props);
    }
}
