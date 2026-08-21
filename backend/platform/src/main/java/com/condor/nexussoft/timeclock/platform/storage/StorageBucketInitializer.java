package com.condor.nexussoft.timeclock.platform.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketEncryptionArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.SseConfiguration;
import io.minio.messages.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Prepara el bucket de evidencias al arrancar: lo crea si falta, activa el cifrado en reposo
 * (SSE-S3, RNF-08) y fija la política de retención. Es idempotente, así que puede ejecutarse
 * en cada arranque y con varias réplicas.
 *
 * <p>Se hace desde Java en vez de con un contenedor {@code mc} auxiliar para no añadir un
 * servicio más al stack de Portainer.
 *
 * <p>Si el cifrado no puede aplicarse (típicamente porque MinIO arrancó sin
 * {@code MINIO_KMS_SECRET_KEY}), se registra un WARN explícito y el arranque continúa: dejar el
 * backend caído por esto bloquearía todos los fichajes, pero el aviso no puede pasar inadvertido
 * porque CA2 de HU-13 exige el cifrado.
 */
@Component
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StorageBucketInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StorageBucketInitializer.class);

    private final MinioClient client;
    private final StorageProperties props;

    public StorageBucketInitializer(MinioClient minioInternalClient, StorageProperties props) {
        this.client = minioInternalClient;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureBucket();
        } catch (Exception e) {
            // No abortamos el arranque: sin bucket, las políticas que exigen foto fallan cerrado
            // y el resto de la plataforma sigue operando.
            log.error("No se pudo preparar el bucket de evidencias '{}': {}", props.bucket(), e.getMessage());
            return;
        }
        applyEncryption();
        applyLifecycle();
    }

    private void ensureBucket() throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(props.bucket()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(props.bucket()).build());
            log.info("Bucket de evidencias '{}' creado.", props.bucket());
        }
    }

    private void applyEncryption() {
        if (props.sse() != StorageProperties.Sse.SSE_S3) {
            log.warn("Cifrado en reposo DESACTIVADO para '{}' (storage.minio.sse=NONE). "
                    + "RNF-08 exige cifrar las evidencias: usa cifrado de disco o activa SSE-S3.", props.bucket());
            return;
        }
        try {
            client.setBucketEncryption(SetBucketEncryptionArgs.builder()
                    .bucket(props.bucket())
                    .config(SseConfiguration.newConfigWithSseS3Rule())
                    .build());
            log.info("Cifrado en reposo SSE-S3 activo en el bucket '{}'.", props.bucket());
        } catch (Exception e) {
            log.warn("NO se pudo activar SSE-S3 en '{}': {}. Las evidencias se guardarán SIN cifrar "
                    + "(¿falta MINIO_KMS_SECRET_KEY en el contenedor de MinIO?).", props.bucket(), e.getMessage());
        }
    }

    private void applyLifecycle() {
        if (props.retentionDays() <= 0) {
            return;
        }
        try {
            LifecycleRule rule = new LifecycleRule(
                    Status.ENABLED, null,
                    new Expiration((ZonedDateTime) null, props.retentionDays(), null),
                    new RuleFilter(""), "expire-evidence", null, null, null);
            client.setBucketLifecycle(SetBucketLifecycleArgs.builder()
                    .bucket(props.bucket())
                    .config(new LifecycleConfiguration(List.of(rule)))
                    .build());
            log.info("Retención de evidencias fijada en {} días para '{}'.", props.retentionDays(), props.bucket());
        } catch (Exception e) {
            log.warn("No se pudo fijar la retención del bucket '{}': {}", props.bucket(), e.getMessage());
        }
    }
}
