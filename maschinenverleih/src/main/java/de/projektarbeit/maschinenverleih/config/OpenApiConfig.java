package de.projektarbeit.maschinenverleih.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Allgemeine Angaben fuer die Swagger-Oberflaeche (http://localhost:8080/swagger-ui.html).
 * Die Endpunkte selbst erkennt springdoc automatisch an den Controllern.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maschinenverleihApi() {
        return new OpenAPI().info(new Info()
                .title("Maschinenverleih API")
                .version("0.1.0")
                .description("REST-API fuer Maschinenstammdaten (US 2) sowie Verfuegbarkeitssuche "
                        + "und Reservierung (US 1)."));
    }
}
