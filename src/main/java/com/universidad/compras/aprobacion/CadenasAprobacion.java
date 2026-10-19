package com.universidad.compras.aprobacion;

import java.util.List;

/**
 * Único lugar donde se decide qué niveles existen y en qué orden se consultan.
 * Agregar, quitar o reordenar un nivel se hace aquí, sin tocar
 * ControladorSolicitudes ni las clases de los demás niveles.
 */
public final class CadenasAprobacion {

    private CadenasAprobacion() {
    }

    /** Supervisor de Área → Gerente de Área → Director Financiero. */
    public static NivelAprobacion estandar() {
        return NivelAprobacion.encadenar(List.of(
                new SupervisorArea(),
                new GerenteArea(),
                new DirectorFinanciero()));
    }

    /** Revisor de Cumplimiento Normativo → Supervisor → Gerente → Director. */
    public static NivelAprobacion conCumplimientoNormativo() {
        return NivelAprobacion.encadenar(List.of(
                new RevisorCumplimientoNormativo(),
                new SupervisorArea(),
                new GerenteArea(),
                new DirectorFinanciero()));
    }
}
