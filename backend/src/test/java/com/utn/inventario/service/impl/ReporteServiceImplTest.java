package com.utn.inventario.service.impl;

import com.utn.inventario.dto.response.ReporteInventarioResponse;
import com.utn.inventario.entity.Producto;
import com.utn.inventario.repository.OrdenCompraRepository;
import com.utn.inventario.repository.ProductoRepository;
import com.utn.inventario.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @InjectMocks
    private ReporteServiceImpl servicio;

    // Precios con centavos elegidos a proposito: con double, 0.10 * 3 + 0.20 * 3 da 0.9000000000000001.
    private final Producto caramelo = producto("0.10", "0.05", 3, 1);
    private final Producto chicle = producto("0.20", "0.10", 3, 5);
    private final Producto gaseosa = producto("1500.55", "1000.00", 2, 2);

    private static Producto producto(String precio, String costo, int stock, int minimo) {
        return Producto.builder().codigo("C").nombre("N").precio(new BigDecimal(precio))
                .precioCosto(new BigDecimal(costo)).stock(stock).stockMinimo(minimo).activo(true).build();
    }

    @BeforeEach
    void setUp() {
        when(productoRepository.findAllActivos()).thenReturn(List.of(caramelo, chicle, gaseosa));
        when(productoRepository.findConStockBajo()).thenReturn(List.of(chicle, gaseosa));
        lenient().when(ventaRepository.sumarTotalDesde(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void cuentaLosProductosActivos() {
        // Act
        ReporteInventarioResponse reporte = servicio.generarReporteInventario();

        // Assert
        assertThat(reporte.getTotalProductos()).isEqualTo(3);
    }

    @Test
    void sumaLasUnidadesEnStock() {
        // Act
        ReporteInventarioResponse reporte = servicio.generarReporteInventario();

        // Assert
        assertThat(reporte.getTotalUnidadesEnStock()).isEqualTo(8);
    }

    @Test
    void cuentaLosProductosConStockBajo() {
        // Act
        ReporteInventarioResponse reporte = servicio.generarReporteInventario();

        // Assert
        assertThat(reporte.getProductosConStockBajo()).isEqualTo(2);
        assertThat(reporte.getDetalleStockBajo()).hasSize(2);
    }

    @Test
    void laValorizacionUsaBigDecimalYEsExacta() {
        // Act
        ReporteInventarioResponse reporte = servicio.generarReporteInventario();

        // Assert: 0.10*3 + 0.20*3 + 1500.55*2 = 3002.00 exacto.
        assertThat(reporte.getValorTotalInventario().compareTo(new BigDecimal("3002.00"))).isZero();
    }

    @Test
    void laValorizacionACostoYLaGananciaSonExactas() {
        // Act
        ReporteInventarioResponse reporte = servicio.generarReporteInventario();

        // Assert: costo 0.05*3 + 0.10*3 + 1000*2 = 2000.45; ganancia 3002.00 - 2000.45 = 1001.55.
        assertThat(reporte.getValorInventarioCosto().compareTo(new BigDecimal("2000.45"))).isZero();
        assertThat(reporte.getGananciaPotencial().compareTo(new BigDecimal("1001.55"))).isZero();
    }
}
