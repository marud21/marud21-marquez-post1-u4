package com.universidad.compras.ejecucion;

import com.universidad.compras.modelo.Solicitud;

/** Reserva el monto de la solicitud en su centro de costo; deshacer lo libera. */
public class ReservarPresupuestoOperacion implements OperacionSolicitud {

    private final PresupuestoService presupuestoService;
    private final Solicitud solicitud;
    private boolean reservado;

    public ReservarPresupuestoOperacion(PresupuestoService presupuestoService, Solicitud solicitud) {
        this.presupuestoService = presupuestoService;
        this.solicitud = solicitud;
    }

    @Override
    public String getSolicitudId() {
        return solicitud.getId();
    }

    @Override
    public String getNombre() {
        return "Reservar presupuesto " + solicitud.getCentroCosto() + " ($" + solicitud.getMonto() + ")";
    }

    @Override
    public void ejecutar() {
        if (reservado) {
            throw new IllegalStateException("El presupuesto de " + solicitud.getId() + " ya está reservado");
        }
        if (!presupuestoService.reservar(solicitud.getCentroCosto(), solicitud.getMonto())) {
            throw new IllegalStateException("No fue posible reservar presupuesto para " + solicitud.getId());
        }
        reservado = true;
    }

    @Override
    public void deshacer() {
        if (!reservado) {
            throw new IllegalStateException("No hay presupuesto reservado que liberar para " + solicitud.getId());
        }
        presupuestoService.liberar(solicitud.getCentroCosto(), solicitud.getMonto());
        reservado = false;
    }

    public boolean isReservado() {
        return reservado;
    }
}
