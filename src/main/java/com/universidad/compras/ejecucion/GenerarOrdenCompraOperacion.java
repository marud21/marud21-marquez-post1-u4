package com.universidad.compras.ejecucion;

import com.universidad.compras.estado.ContextoSolicitud;
import com.universidad.compras.estado.ResultadoTransicion;
import com.universidad.compras.modelo.Solicitud;
import com.universidad.compras.notificacion.PublicadorCambiosEstado;

/**
 * Genera la orden de compra con el proveedor y deja la solicitud EJECUTADA;
 * deshacer cancela la orden y devuelve la solicitud al estado que tenía antes.
 * Si el estado actual no permite ejecutar, no se contacta al proveedor.
 */
public class GenerarOrdenCompraOperacion implements OperacionSolicitud {

    private final OrdenCompraService ordenCompraService;
    private final Solicitud solicitud;
    private final String proveedor;
    private final ContextoSolicitud contexto;
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
        this.contexto = new ContextoSolicitud(solicitud, publicador);
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
        String estadoPrevio = solicitud.getEstado();
        ResultadoTransicion transicion = contexto.ejecutar("Orden de compra generada con " + proveedor);
        if (!transicion.exitosa()) {
            throw new IllegalStateException(transicion.mensaje());
        }
        try {
            numeroOrden = ordenCompraService.generar(solicitud.getId(), proveedor);
        } catch (RuntimeException e) {
            contexto.revertirA(estadoPrevio, "Falló la generación de la orden: " + e.getMessage());
            throw e;
        }
        estadoAnterior = estadoPrevio;
    }

    @Override
    public void deshacer() {
        if (numeroOrden == null) {
            throw new IllegalStateException("No hay orden de compra generada que cancelar para " + solicitud.getId());
        }
        ordenCompraService.cancelar(numeroOrden);
        contexto.revertirA(estadoAnterior, "Orden de compra " + numeroOrden + " cancelada");
        numeroOrden = null;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }
}
