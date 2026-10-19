package com.universidad.compras.estado;

/** Estado final: no admite ninguna operación. */
public class EstadoRechazada extends EstadoBase {

    public static final EstadoRechazada INSTANCIA = new EstadoRechazada();

    private EstadoRechazada() {
    }

    @Override
    public String nombre() {
        return "RECHAZADA";
    }
}
