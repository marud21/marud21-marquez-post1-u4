package com.universidad.compras.ejecucion;

import com.universidad.compras.modelo.Solicitud;

/** Crea las operaciones ya conectadas a sus servicios de bajo nivel. */
public class FabricaOperacionesSolicitud {

    private final PresupuestoService presupuestoService;
    private final OrdenCompraService ordenCompraService;

    public FabricaOperacionesSolicitud(PresupuestoService presupuestoService, OrdenCompraService ordenCompraService) {
        this.presupuestoService = presupuestoService;
        this.ordenCompraService = ordenCompraService;
    }

    public ReservarPresupuestoOperacion reservarPresupuesto(Solicitud solicitud) {
        return new ReservarPresupuestoOperacion(presupuestoService, solicitud);
    }

    public GenerarOrdenCompraOperacion generarOrdenCompra(Solicitud solicitud, String proveedor) {
        return new GenerarOrdenCompraOperacion(ordenCompraService, solicitud, proveedor);
    }
}
