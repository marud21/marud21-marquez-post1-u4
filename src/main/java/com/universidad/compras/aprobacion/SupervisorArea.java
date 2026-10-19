package com.universidad.compras.aprobacion;

/** Aprueba solicitudes de hasta $2.000.000. */
public class SupervisorArea extends NivelConLimite {

    public static final double LIMITE = 2_000_000;

    public SupervisorArea() {
        super(LIMITE);
    }

    @Override
    public String getNombre() {
        return "Supervisor de Área";
    }
}
