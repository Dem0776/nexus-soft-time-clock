package com.condor.nexussoft.timeclock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración de humo: levanta la aplicación completa contra una instancia
 * real de PostGIS (Testcontainers), ejecutando TODAS las migraciones Flyway (V1..V12),
 * y verifica que el contexto carga y los endpoints públicos responden.
 * <p>
 * Requiere Docker en ejecución (CI). Se ejecuta en la fase de integración (nombre *IT),
 * no en las pruebas unitarias.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // La caché usa NoOp en el test; Redis no es necesario para el humo.
                "spring.cache.type=none",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
                "outbox.relay-delay-ms=60000"
        })
class ApplicationSmokeIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGIS = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("nexus")
            .withUsername("nexus")
            .withPassword("nexus");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextoCarga_yPingResponde() {
        ResponseEntity<String> ping = restTemplate.getForEntity("/api/v1/ping", String.class);
        assertThat(ping.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ping.getBody()).contains("UP");
    }

    @Test
    void healthEndpoint_estaUp() {
        ResponseEntity<String> health = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
