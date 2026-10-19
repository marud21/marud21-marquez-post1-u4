package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;

/** Nivel cuya autoridad está acotada por un monto máximo (inclusive). */
public abstract class NivelConLimite extends NivelAprobacion {

    private final double montoMaximo;

    protected NivelConLimite(double montoMaximo) {
        this.montoMaximo = montoMaximo;
    }

    public double getMontoMaximo() {
        return montoMaximo;
    }

    @Override
    protected boolean puedeResolver(Solicitud solicitud) {
        return solicitud.getMonto() <= montoMaximo;
    }
}
