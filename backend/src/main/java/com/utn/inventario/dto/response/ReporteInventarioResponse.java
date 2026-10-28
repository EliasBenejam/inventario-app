package com.utn.inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteInventarioResponse {
    private long totalProductos;
    private long totalUnidadesEnStock;
    /** Stock valorizado a precio de venta. */
    private BigDecimal valorTotalInventario;
    /** Stock valorizado a precio de costo. */
    private BigDecimal valorInventarioCosto;
    /** Lo que se ganaria vendiendo todo el stock actual (venta - costo). */
    private BigDecimal gananciaPotencial;
    private long productosConStockBajo;
    private long productosSinStock;
    private List<ProductoResponse> detalleStockBajo;
    /** Ventas confirmadas desde el primer dia del mes actual. */
    private BigDecimal ventasDelMes;
    private long cantidadVentasDelMes;
    private long ordenesCompraPendientes;
}
