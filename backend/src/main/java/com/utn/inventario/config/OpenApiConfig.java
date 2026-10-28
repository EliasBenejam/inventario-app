package com.utn.inventario.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion de la API con Swagger UI (http://localhost:8080/swagger-ui.html).
 * Define el esquema "bearerAuth" para poder pegar el JWT con el boton "Authorize".
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API Sistema de Inventario", version = "1.0",
                description = "TP Final - Tecnicatura Universitaria en Programacion (UTN Villa Maria)"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
