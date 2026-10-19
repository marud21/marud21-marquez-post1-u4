package com.universidad.compras.estado;

/** Estado final: no admite ninguna operación. */
public class EstadoCancelada extends EstadoBase {

    public static final EstadoCancelada INSTANCIA = new EstadoCancelada();

    private EstadoCancelada() {
    }

    @Override
    public String nombre() {
        return "CANCELADA";
    }
}
