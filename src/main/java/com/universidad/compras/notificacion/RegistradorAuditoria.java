package com.universidad.compras.notificacion;

import org.springframework.stereotype.Component;

/** Deja constancia de la transición (estado anterior → nuevo) en el log de auditoría. */
@Component
public class RegistradorAuditoria implements ObservadorCambioEstado {

    @Override
    public void alCambiarEstado(EventoCambioEstado evento) {
        ClientesNotificacion.registrarAuditoria(
                evento.solicitudId(),
                evento.estadoNuevo(),
                evento.estadoAnterior() + " -> " + evento.estadoNuevo() + ": " + evento.detalle());
    }
}
