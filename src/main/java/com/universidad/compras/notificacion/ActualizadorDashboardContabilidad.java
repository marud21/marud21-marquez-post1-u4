package com.universidad.compras.notificacion;

import org.springframework.stereotype.Component;

/** Refleja el nuevo estado y el monto en el tablero de contabilidad. */
@Component
public class ActualizadorDashboardContabilidad implements ObservadorCambioEstado {

    @Override
    public void alCambiarEstado(EventoCambioEstado evento) {
        ClientesNotificacion.actualizarDashboardContabilidad(
                evento.solicitudId(), evento.estadoNuevo(), evento.monto());
    }
}
