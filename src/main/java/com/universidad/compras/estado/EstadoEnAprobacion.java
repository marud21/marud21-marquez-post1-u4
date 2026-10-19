package com.universidad.compras.estado;

/** En revisión por la cadena de aprobación: admite las mismas operaciones que PENDIENTE. */
public class EstadoEnAprobacion extends EstadoPendiente {

    public static final EstadoEnAprobacion INSTANCIA = new EstadoEnAprobacion();

    private EstadoEnAprobacion() {
    }

    @Override
    public String nombre() {
        return "EN_APROBACION";
    }
}
