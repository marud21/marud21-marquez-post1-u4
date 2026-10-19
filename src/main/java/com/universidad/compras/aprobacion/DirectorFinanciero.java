package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;

/** Último eslabón: resuelve cualquier monto, sin límite superior. */
public class DirectorFinanciero extends NivelAprobacion {

    @Override
    public String getNombre() {
        return "Director Financiero";
    }

    @Override
    protected boolean puedeResolver(Solicitud solicitud) {
        return true;
    }
}
