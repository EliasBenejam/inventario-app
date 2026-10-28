package com.utn.inventario.controller;

import com.utn.inventario.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Validaciones de POST /api/productos: el error indica que campo fallo. */
class ProductoControllerTest extends ApiTestBase {

    @Test
    void crearProductoSinCodigoDevuelve400ConElDetalleDelCampo() throws Exception {
        // Arrange
        Map<String, Object> sinCodigo = Map.of("nombre", "Galletitas", "precio", 900);

        // Act
        ResultActions respuesta = post("/api/productos", tokenAdmin, sinCodigo);

        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"))
                .andExpect(jsonPath("$.detalles", hasItem(startsWith("codigo:"))));
    }

    @Test
    void crearProductoSinNombreDevuelve400ConElDetalleDelCampo() throws Exception {
        // Arrange
        Map<String, Object> sinNombre = Map.of("codigo", unico("GAL-"), "precio", 900);

        // Act
        ResultActions respuesta = post("/api/productos", tokenAdmin, sinNombre);

        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"))
                .andExpect(jsonPath("$.detalles", hasItem(startsWith("nombre:"))));
    }

    @Test
    void crearProductoSinCodigoNiNombreInformaAmbosCampos() throws Exception {
        // Arrange
        Map<String, Object> vacio = Map.of("precio", 900);

        // Act
        ResultActions respuesta = post("/api/productos", tokenAdmin, vacio);

        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem(startsWith("codigo:"))))
                .andExpect(jsonPath("$.detalles", hasItem(startsWith("nombre:"))));
    }
}
