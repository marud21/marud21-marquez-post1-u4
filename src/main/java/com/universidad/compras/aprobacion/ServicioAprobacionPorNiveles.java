package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;
import com.universidad.compras.notificacion.EventoCambioEstado;
import com.universidad.compras.notificacion.PublicadorCambiosEstado;

/**
 * Implementación de {@link ServicioAprobacion} que entrega la solicitud al
 * primer eslabón de la cadena. No sabe cuántos niveles existen, en qué orden
 * se consultan ni si una categoría requiere un nivel adicional.
 */
public class ServicioAprobacionPorNiveles implements ServicioAprobacion {

    private final NivelAprobacion primerNivel;
    private final PublicadorCambiosEstado publicador;

    public ServicioAprobacionPorNiveles(NivelAprobacion primerNivel) {
        this(primerNivel, new PublicadorCambiosEstado());
    }

    public ServicioAprobacionPorNiveles(NivelAprobacion primerNivel, PublicadorCambiosEstado publicador) {
        this.primerNivel = primerNivel;
        this.publicador = publicador;
    }

    @Override
    public ResultadoAprobacion evaluar(Solicitud solicitud) {
        ResultadoAprobacion resultado = primerNivel.manejar(solicitud);
        String estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(resultado.isAprobada() ? "APROBADA" : "RECHAZADA");
        publicador.publicar(EventoCambioEstado.de(solicitud, estadoAnterior, resultado.getDetalle()));
        return resultado;
    }
}
