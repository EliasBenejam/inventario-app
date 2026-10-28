package com.utn.inventario.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.utn.inventario.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo de stock de punta a punta y validaciones de /api/movimientos. */
class MovimientoControllerTest extends ApiTestBase {

    /**
     * Crear producto -> entrada -> verificar stock e historial ->
     * salida excesiva -> 400 y el stock queda intacto.
     */
    @Test
    void flujoCompletoDeStockConSalidaExcesivaRechazada() throws Exception {
        // Arrange: producto nuevo con 10 unidades.
        long id = crearProducto("500", 10);

        // Act 1: entrada de 5.
        ResultActions entrada = post("/api/movimientos/entrada", tokenEmpleado,
                Map.of("productoId", id, "cantidad", 5, "motivo", "compra"));

        // Assert 1: stock 15 y la entrada queda en el historial.
        entrada.andExpect(status().isOk()).andExpect(jsonPath("$.stockResultante").value(15));
        assertThat(stock(id)).isEqualTo(15);
        JsonNode historial = movimientos(id);
        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).get("tipo").asText()).isEqualTo("ENTRADA");
        assertThat(historial.get(0).get("usuarioNombre").asText()).isEqualTo("Empleado Demo");

        // Act 2: salida mayor al stock disponible.
        ResultActions salida = post("/api/movimientos/salida", tokenEmpleado,
                Map.of("productoId", id, "cantidad", 16));

        // Assert 2: 400, el stock sigue en 15 y no se agrego ningun movimiento.
        salida.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));
        assertThat(stock(id)).isEqualTo(15);
        assertThat(movimientos(id)).hasSize(1);
    }

    @ParameterizedTest(name = "cantidad {0}")
    @ValueSource(ints = {0, -1, -50})
    void salidaConCantidadCeroONegativaDevuelve400(int cantidad) throws Exception {
        // Arrange
        long id = crearProducto("500", 10);

        // Act
        ResultActions respuesta = post("/api/movimientos/salida", tokenEmpleado,
                Map.of("productoId", id, "cantidad", cantidad));

        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.startsWith("cantidad:")));
        assertThat(stock(id)).isEqualTo(10);
    }
}
