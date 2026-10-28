package com.utn.inventario.entity;

/**
 * Roles de acceso al sistema.
 * ADMIN: acceso total (usuarios, categorias, proveedores, productos, movimientos, reportes).
 * EMPLEADO: acceso operativo (productos, movimientos de stock, consultas), sin gestion de usuarios.
 */
public enum RolUsuario {
    ADMIN,
    EMPLEADO
}
