package com.universidad.compras.notificacion;

import org.springframework.stereotype.Component;

/** Avisa por correo al solicitante del nuevo estado de su solicitud. */
@Component
public class NotificadorCorreoSolicitante implements ObservadorCambioEstado {

    @Override
    public void alCambiarEstado(EventoCambioEstado evento) {
        ClientesNotificacion.enviarCorreo(
                evento.solicitanteEmail(),
                "Solicitud " + evento.solicitudId() + " " + evento.estadoNuevo(),
                evento.detalle());
    }
}
