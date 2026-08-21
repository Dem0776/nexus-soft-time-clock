package com.condor.nexussoft.timeclock.platform.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Siempre debe existir un {@link ObjectStorage} inyectable: quienes lo consumen no lo declaran
 * opcional, así que dejar el contenedor sin ese bean impide arrancar la aplicación entera.
 */
class StorageConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StorageConfig.class))
            .withPropertyValues(
                    "storage.minio.endpoint=http://localhost:9000",
                    "storage.minio.public-endpoint=http://localhost:9000",
                    "storage.minio.access-key=test",
                    "storage.minio.secret-key=test",
                    "storage.minio.bucket=evidence",
                    "storage.minio.region=us-east-1");

    @Test
    void porDefecto_seUsaElAlmacenamientoReal() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(ObjectStorage.class)
                .getBean(ObjectStorage.class).isInstanceOf(MinioObjectStorage.class));
    }

    @Test
    void almacenamientoDesactivado_elContextoArrancaConElSustituto() {
        runner.withPropertyValues("storage.minio.enabled=false")
                .run(context -> assertThat(context)
                        .hasSingleBean(ObjectStorage.class)
                        .getBean(ObjectStorage.class).isInstanceOf(DisabledObjectStorage.class));
    }

    /** El sustituto falla cerrado: sin poder comprobar la evidencia, se considera inexistente. */
    @Test
    void elSustituto_nuncaConfirmaQueExistaUnObjeto() {
        runner.withPropertyValues("storage.minio.enabled=false").run(context -> {
            ObjectStorage storage = context.getBean(ObjectStorage.class);
            assertThat(storage.isAvailable()).isFalse();
            assertThat(storage.stat("t/x/y.jpg")).isEmpty();
            assertThat(storage.bucket()).isEqualTo("evidence");
        });
    }
}
