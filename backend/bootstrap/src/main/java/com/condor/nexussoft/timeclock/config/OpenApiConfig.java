package com.condor.nexussoft.timeclock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI / Swagger UI. La API se versiona por ruta (/api/v1).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI timeClockOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Nexus Soft Time Clock API")
                .description("API REST de control de asistencia multi-tenant (QR + GPS + geocercas + antifraude).")
                .version("v1")
                .contact(new Contact().name("Nexus Soft"))
                .license(new License().name("Proprietary")));
    }
}
