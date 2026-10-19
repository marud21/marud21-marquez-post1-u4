package com.universidad.compras.ejecucion;

import com.universidad.compras.modelo.Solicitud;
import com.universidad.compras.notificacion.EventoCambioEstado;
import com.universidad.compras.notificacion.PublicadorCambiosEstado;

/**
 * Genera la orden de compra con el proveedor y deja la solicitud EJECUTADA;
 * deshacer cancela la orden y devuelve la solicitud al estado que tenía antes.
 */
public class GenerarOrdenCompraOperacion implements OperacionSolicitud {

    private final OrdenCompraService ordenCompraService;
    private final Solicitud solicitud;
    private final String proveedor;
    private final PublicadorCambiosEstado publicador;
    private String numeroOrden;
    private String estadoAnterior;

    public GenerarOrdenCompraOperacion(OrdenCompraService ordenCompraService, Solicitud solicitud, String proveedor) {
        this(ordenCompraService, solicitud, proveedor, new PublicadorCambiosEstado());
    }

    public GenerarOrdenCompraOperacion(OrdenCompraService ordenCompraService, Solicitud solicitud, String proveedor,
                                       PublicadorCambiosEstado publicador) {
        this.ordenCompraService = ordenCompraService;
        this.solicitud = solicitud;
        this.proveedor = proveedor;
        this.publicador = publicador;
    }

    @Override
    public String getSolicitudId() {
        return solicitud.getId();
    }

    @Override
    public String getNombre() {
        return "Generar orden de compra con " + proveedor;
    }

    @Override
    public void ejecutar() {
        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new IllegalStateException("Solo se puede generar la orden de una solicitud APROBADA; "
                    + solicitud.getId() + " está " + solicitud.getEstado());
        }
        estadoAnterior = solicitud.getEstado();
        numeroOrden = ordenCompraService.generar(solicitud.getId(), proveedor);
        solicitud.setEstado("EJECUTADA");
        publicador.publicar(EventoCambioEstado.de(solicitud, estadoAnterior,
                "Orden de compra " + numeroOrden + " generada con " + proveedor));
    }

    @Override
    public void deshacer() {
        if (numeroOrden == null) {
            throw new IllegalStateException("No hay orden de compra generada que cancelar para " + solicitud.getId());
        }
        ordenCompraService.cancelar(numeroOrden);
        String estadoActual = solicitud.getEstado();
        solicitud.setEstado(estadoAnterior);
        publicador.publicar(EventoCambioEstado.de(solicitud, estadoActual,
                "Orden de compra " + numeroOrden + " cancelada"));
        numeroOrden = null;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }
}
