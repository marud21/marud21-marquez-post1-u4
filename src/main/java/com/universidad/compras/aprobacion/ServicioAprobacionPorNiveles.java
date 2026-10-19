package com.universidad.compras.aprobacion;

import com.universidad.compras.estado.ContextoSolicitud;
import com.universidad.compras.estado.ResultadoTransicion;
import com.universidad.compras.modelo.Solicitud;
import com.universidad.compras.notificacion.PublicadorCambiosEstado;

/**
 * Implementación de {@link ServicioAprobacion} que entrega la solicitud al
 * primer eslabón de la cadena. No sabe cuántos niveles existen, en qué orden
 * se consultan ni si una categoría requiere un nivel adicional. La decisión de
 * la cadena se aplica a través del estado de la solicitud, que valida la
 * transición y notifica a los suscriptores.
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
        ContextoSolicitud contexto = new ContextoSolicitud(solicitud, publicador);
        ResultadoTransicion transicion = resultado.isAprobada()
                ? contexto.aprobar(resultado.getDetalle())
                : contexto.rechazar(resultado.getDetalle());
        if (!transicion.exitosa()) {
            return new ResultadoAprobacion(false, resultado.getNivelResolutor(), transicion.mensaje());
        }
        return resultado;
    }
}
