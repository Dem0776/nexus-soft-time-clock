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
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StorageConfig {

    @Bean
    public MinioClient minioInternalClient(StorageProperties props) {
        return MinioClient.builder()
                .endpoint(props.endpoint())
                .credentials(props.accessKey(), props.secretKey())
                .region(props.region())
                .build();
    }

    @Bean
    public MinioClient minioSigningClient(StorageProperties props) {
        return MinioClient.builder()
                .endpoint(props.signingEndpoint())
                .credentials(props.accessKey(), props.secretKey())
                .region(props.region())
                .build();
    }

    @Bean
    public ObjectStorage objectStorage(MinioClient minioInternalClient, MinioClient minioSigningClient,
                                       StorageProperties props) {
        return new MinioObjectStorage(minioInternalClient, minioSigningClient, props);
    }
}
