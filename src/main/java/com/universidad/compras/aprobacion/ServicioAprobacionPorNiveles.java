package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;

/**
 * Implementación de {@link ServicioAprobacion} que entrega la solicitud al
 * primer eslabón de la cadena. No sabe cuántos niveles existen, en qué orden
 * se consultan ni si una categoría requiere un nivel adicional.
 */
public class ServicioAprobacionPorNiveles implements ServicioAprobacion {

    private final NivelAprobacion primerNivel;

    public ServicioAprobacionPorNiveles(NivelAprobacion primerNivel) {
        this.primerNivel = primerNivel;
    }

    @Override
    public ResultadoAprobacion evaluar(Solicitud solicitud) {
        ResultadoAprobacion resultado = primerNivel.manejar(solicitud);
        solicitud.setEstado(resultado.isAprobada() ? "APROBADA" : "RECHAZADA");
        return resultado;
    }
}
