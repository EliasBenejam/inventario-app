package com.utn.inventario.service.impl;

import com.utn.inventario.dto.request.ProductoRequest;
import com.utn.inventario.dto.response.ProductoResponse;
import com.utn.inventario.entity.Producto;
import com.utn.inventario.exception.BusinessException;
import com.utn.inventario.exception.ResourceNotFoundException;
import com.utn.inventario.repository.CategoriaRepository;
import com.utn.inventario.repository.ProductoRepository;
import com.utn.inventario.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProductoServiceImpl servicio;

    private Producto existente;

    @BeforeEach
    void setUp() {
        existente = Producto.builder().id(1L).codigo("ARR-001").nombre("Arroz")
                .precio(new BigDecimal("1200")).precioCosto(new BigDecimal("850"))
                .stock(7).stockMinimo(3).activo(true).build();
        lenient().when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static ProductoRequest request(String codigo) {
        ProductoRequest request = new ProductoRequest();
        request.setCodigo(codigo);
        request.setNombre("Arroz largo fino");
        request.setPrecio(new BigDecimal("1300"));
        return request;
    }

    // --------------------------------------------------------------- crear

    @Test
    void crearConUnCodigoYaExistenteLanzaExcepcionYNoGuarda() {
        // Arrange
        when(productoRepository.existsByCodigoIgnoreCase("ARR-001")).thenReturn(true);
        ProductoRequest pedido = request("ARR-001");

        // Act + Assert
        assertThatThrownBy(() -> servicio.crear(pedido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ARR-001");
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crearConCategoriaInexistenteLanzaResourceNotFoundException() {
        // Arrange
        when(productoRepository.existsByCodigoIgnoreCase(anyString())).thenReturn(false);
        when(categoriaRepository.findById(50L)).thenReturn(Optional.empty());
        ProductoRequest pedido = request("NUE-001");
        pedido.setCategoriaId(50L);

        // Act + Assert
        assertThatThrownBy(() -> servicio.crear(pedido))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoría");
    }

    @Test
    void crearConProveedorInexistenteLanzaResourceNotFoundException() {
        // Arrange
        when(productoRepository.existsByCodigoIgnoreCase(anyString())).thenReturn(false);
        when(proveedorRepository.findById(60L)).thenReturn(Optional.empty());
        ProductoRequest pedido = request("NUE-002");
        pedido.setProveedorId(60L);

        // Act + Assert
        assertThatThrownBy(() -> servicio.crear(pedido))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proveedor");
    }

    // ----------------------------------------------------------- actualizar

    @Test
    void actualizarNoModificaElStockDelProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        ProductoRequest pedido = request("ARR-001");
        pedido.setStockInicial(999);

        // Act
        ProductoResponse respuesta = servicio.actualizar(1L, pedido);

        // Assert
        assertThat(existente.getStock()).isEqualTo(7);
        assertThat(respuesta.getStock()).isEqualTo(7);
    }

    @Test
    void actualizarPermiteConservarElMismoCodigoDelPropioProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        ProductoRequest pedido = request("ARR-001");

        // Act
        ProductoResponse respuesta = servicio.actualizar(1L, pedido);

        // Assert
        assertThat(respuesta.getCodigo()).isEqualTo("ARR-001");
        assertThat(respuesta.getNombre()).isEqualTo("Arroz largo fino");
        verify(productoRepository, never()).existsByCodigoIgnoreCase(anyString());
    }

    @Test
    void actualizarConElCodigoDeOtroProductoLanzaExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.existsByCodigoIgnoreCase("FID-001")).thenReturn(true);
        ProductoRequest pedido = request("FID-001");

        // Act + Assert
        assertThatThrownBy(() -> servicio.actualizar(1L, pedido))
                .isInstanceOf(BusinessException.class);
        verify(productoRepository, never()).save(any());
    }

    // ------------------------------------------------------------- eliminar

    @Test
    void eliminarHaceBajaLogicaYNoBorraElRegistro() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        servicio.eliminar(1L);

        // Assert
        assertThat(existente.isActivo()).isFalse();
        verify(productoRepository).save(existente);
        verify(productoRepository, never()).delete(any());
        verify(productoRepository, never()).deleteById(any());
    }

    // ------------------------------------------------------------ stockBajo

    @ParameterizedTest(name = "stock {0} / minimo {1} -> stockBajo {2}")
    @CsvSource({
            "2, 3, true",   // por debajo del minimo
            "3, 3, true",   // igual al minimo: tambien cuenta como stock bajo
            "4, 3, false"   // por encima del minimo
    })
    void elFlagStockBajoEsVerdaderoCuandoElStockEsMenorOIgualAlMinimo(int stock, int minimo, boolean esperado) {
        // Arrange
        existente.setStock(stock);
        existente.setStockMinimo(minimo);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        ProductoResponse respuesta = servicio.obtenerPorId(1L);

        // Assert
        assertThat(respuesta.isStockBajo()).isEqualTo(esperado);
    }
}
