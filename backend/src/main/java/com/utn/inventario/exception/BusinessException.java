package com.utn.inventario.exception;

/**
 * Excepcion para violaciones de reglas de negocio
 * (ej: stock insuficiente, codigo duplicado, credenciales invalidas).
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
