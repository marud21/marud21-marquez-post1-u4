package com.universidad.compras.notificacion;

import com.universidad.compras.modelo.Solicitud;

/**
 * Lo que los suscriptores reciben cuando una solicitud cambia de estado. Es una
 * copia inmutable: ningún suscriptor puede alterar la solicitud original.
 */
public record EventoCambioEstado(String solicitudId,
                                 String solicitanteEmail,
                                 double monto,
                                 String estadoAnterior,
                                 String estadoNuevo,
                                 String detalle) {

    public static EventoCambioEstado de(Solicitud solicitud, String estadoAnterior, String detalle) {
        return new EventoCambioEstado(solicitud.getId(), solicitud.getSolicitanteEmail(), solicitud.getMonto(),
                estadoAnterior, solicitud.getEstado(), detalle);
    }
}
