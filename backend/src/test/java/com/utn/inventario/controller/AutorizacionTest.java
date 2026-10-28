package com.utn.inventario.controller;

import com.utn.inventario.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Quien puede entrar a que: sin token (401), EMPLEADO en zona de ADMIN (403) y ADMIN (200).
 * Tambien verifica que los errores 401 y 403 vengan como JSON con la forma de ErrorResponse.
 */
class AutorizacionTest extends ApiTestBase {

    @Test
    void endpointProtegidoSinTokenDevuelve401() throws Exception {
        // Act
        ResultActions respuesta = get("/api/productos", null);

        // Assert
        respuesta.andExpect(status().isUnauthorized());
    }

    @Test
    void elError401VieneEnJsonConLaEstructuraDeErrorResponse() throws Exception {
        // Act
        ResultActions respuesta = get("/api/productos", null);

        // Assert
        respuesta.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/productos"));
    }

    @Test
    void unEmpleadoPidiendoLaListaDeUsuariosRecibe403() throws Exception {
        // Act
        ResultActions respuesta = get("/api/usuarios", tokenEmpleado);

        // Assert
        respuesta.andExpect(status().isForbidden());
    }

    @Test
    void elError403VieneEnJsonConLaEstructuraDeErrorResponse() throws Exception {
        // Act
        ResultActions respuesta = get("/api/usuarios", tokenEmpleado);

        // Assert
        respuesta.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/usuarios"));
    }

    @Test
    void unAdminPidiendoLaListaDeUsuariosRecibe200() throws Exception {
        // Act
        ResultActions respuesta = get("/api/usuarios", tokenAdmin);

        // Assert
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
