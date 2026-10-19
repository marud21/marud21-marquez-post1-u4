package com.universidad.compras.estado;

/**
 * Comportamiento de una solicitud según el estado en que se encuentra. Cada
 * estado decide qué operaciones acepta y hacia qué estado transiciona; una
 * operación no permitida se rechaza sin cambiar el estado.
 */
public interface EstadoSolicitud {

    /** Nombre persistido en {@code Solicitud.estado}. */
    String nombre();

    ResultadoTransicion aprobar(ContextoSolicitud contexto, String motivo);

    ResultadoTransicion rechazar(ContextoSolicitud contexto, String motivo);

    ResultadoTransicion ejecutar(ContextoSolicitud contexto, String motivo);

    ResultadoTransicion cancelar(ContextoSolicitud contexto, String motivo);
}
