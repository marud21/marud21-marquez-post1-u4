package com.universidad.compras.estado;

/** Resultado de intentar una operación sobre la solicitud en su estado actual. */
public record ResultadoTransicion(boolean exitosa, String estado, String mensaje) {

    public static ResultadoTransicion exitosa(String estadoNuevo, String mensaje) {
        return new ResultadoTransicion(true, estadoNuevo, mensaje);
    }

    public static ResultadoTransicion invalida(String estadoActual, String mensaje) {
        return new ResultadoTransicion(false, estadoActual, mensaje);
    }
}
