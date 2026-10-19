package com.universidad.compras.aprobacion;

/** Aprueba solicitudes de hasta $10.000.000. */
public class GerenteArea extends NivelConLimite {

    public static final double LIMITE = 10_000_000;

    public GerenteArea() {
        super(LIMITE);
    }

    @Override
    public String getNombre() {
        return "Gerente de Área";
    }
}
