package com.utn.inventario.controller;

import com.utn.inventario.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Complementa CategoriaServiceImplTest: el test unitario usa un repositorio simulado,
 * aca se comprueba contra la base real que la consulta IgnoreCase detecta el duplicado.
 */
class CategoriaControllerTest extends ApiTestBase {

    @Test
    void crearCategoriaDuplicadaEnMayusculasDevuelve400ContraLaBaseReal() throws Exception {
        // Arrange: "Bebidas" la siembra DataInitializer.
        Map<String, String> duplicada = Map.of("nombre", "BEBIDAS");

        // Act
        ResultActions respuesta = post("/api/categorias", tokenAdmin, duplicada);

        // Assert
        respuesta.andExpect(status().isBadRequest());
    }
}
