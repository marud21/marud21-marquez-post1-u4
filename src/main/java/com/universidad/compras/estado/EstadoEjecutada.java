package com.universidad.compras.estado;

/**
 * Estado final para las operaciones de negocio. Revertir una ejecución por error
 * se hace deshaciendo la operación de la Necesidad 2, no con una transición.
 */
public class EstadoEjecutada extends EstadoBase {

    public static final EstadoEjecutada INSTANCIA = new EstadoEjecutada();

    private EstadoEjecutada() {
    }

    @Override
    public String nombre() {
        return "EJECUTADA";
    }

    @Override
    public ResultadoTransicion ejecutar(ContextoSolicitud contexto, String motivo) {
        return ResultadoTransicion.invalida(nombre(), "Error: ya fue ejecutada");
    }
}
