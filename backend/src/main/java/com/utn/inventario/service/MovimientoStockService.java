package com.utn.inventario.service;

import com.utn.inventario.dto.request.MovimientoRequest;
import com.utn.inventario.dto.response.MovimientoResponse;
import com.utn.inventario.entity.MovimientoStock;
import com.utn.inventario.entity.Producto;
import com.utn.inventario.entity.TipoMovimiento;
import com.utn.inventario.entity.Usuario;

import java.util.List;

public interface MovimientoStockService {
    MovimientoResponse registrarEntrada(MovimientoRequest request, String emailUsuario);
    MovimientoResponse registrarSalida(MovimientoRequest request, String emailUsuario);
    List<MovimientoResponse> listarTodos();
    List<MovimientoResponse> listarPorProducto(Long productoId);

    /**
     * Actualiza el stock del producto y deja registrado el movimiento.
     * Lo usan tambien las ventas y las ordenes de compra, para que todas las
     * modificaciones de stock pasen por las mismas validaciones.
     */
    MovimientoStock aplicarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad,
                                      String motivo, Usuario usuario);
}
