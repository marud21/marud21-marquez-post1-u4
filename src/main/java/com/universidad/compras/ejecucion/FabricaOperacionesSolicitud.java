package com.universidad.compras.ejecucion;

import com.universidad.compras.modelo.Solicitud;
import com.universidad.compras.notificacion.PublicadorCambiosEstado;

/** Crea las operaciones ya conectadas a sus servicios de bajo nivel y al publicador de cambios. */
public class FabricaOperacionesSolicitud {

    private final PresupuestoService presupuestoService;
    private final OrdenCompraService ordenCompraService;
    private final PublicadorCambiosEstado publicador;

    public FabricaOperacionesSolicitud(PresupuestoService presupuestoService, OrdenCompraService ordenCompraService,
                                       PublicadorCambiosEstado publicador) {
        this.presupuestoService = presupuestoService;
        this.ordenCompraService = ordenCompraService;
        this.publicador = publicador;
    }

    public ReservarPresupuestoOperacion reservarPresupuesto(Solicitud solicitud) {
        return new ReservarPresupuestoOperacion(presupuestoService, solicitud);
    }

    public GenerarOrdenCompraOperacion generarOrdenCompra(Solicitud solicitud, String proveedor) {
        return new GenerarOrdenCompraOperacion(ordenCompraService, solicitud, proveedor, publicador);
    }
}
