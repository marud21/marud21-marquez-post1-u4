package com.universidad.compras.notificacion;

/** Reacción externa ante el cambio de estado de una solicitud. */
@FunctionalInterface
public interface ObservadorCambioEstado {

    void alCambiarEstado(EventoCambioEstado evento);
}
