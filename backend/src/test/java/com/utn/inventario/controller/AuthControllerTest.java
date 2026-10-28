package com.utn.inventario.controller;

import com.utn.inventario.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /api/auth/login contra la aplicacion completa (perfil dev, H2). */
class AuthControllerTest extends ApiTestBase {

    @Test
    void loginConCredencialesValidasDevuelve200YUnToken() throws Exception {
        // Arrange
        Map<String, String> credenciales = Map.of("email", ADMIN_EMAIL, "password", "admin123");

        // Act
        ResultActions respuesta = post("/api/auth/login", null, credenciales);

        // Assert
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void loginConContraseniaIncorrectaDevuelve401() throws Exception {
        // Arrange
        Map<String, String> credenciales = Map.of("email", ADMIN_EMAIL, "password", "incorrecta");

        // Act
        ResultActions respuesta = post("/api/auth/login", null, credenciales);

        // Assert
        respuesta.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }
}
