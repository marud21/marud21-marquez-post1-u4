package com.universidad.compras.estado;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Traduce el nombre persistido en {@code Solicitud.estado} (un String, porque la
 * entidad dada no puede modificarse) al objeto estado correspondiente. Agregar
 * un estado nuevo, por ejemplo EN_ESPERA_PROVEEDOR, es crear su clase y
 * registrarla aquí. No hay if/else que revisar en las operaciones.
 */
public final class CatalogoEstados {

    private static final Map<String, EstadoSolicitud> ESTADOS = registrar(
            EstadoPendiente.INSTANCIA,
            EstadoEnAprobacion.INSTANCIA,
            EstadoAprobada.INSTANCIA,
            EstadoRechazada.INSTANCIA,
            EstadoEjecutada.INSTANCIA,
            EstadoCancelada.INSTANCIA);

    private CatalogoEstados() {
    }

    public static EstadoSolicitud obtener(String nombre) {
        EstadoSolicitud estado = ESTADOS.get(nombre);
        if (estado == null) {
            throw new IllegalArgumentException("Estado de solicitud desconocido: " + nombre);
        }
        return estado;
    }

    public static Set<String> nombres() {
        return ESTADOS.keySet();
    }

    private static Map<String, EstadoSolicitud> registrar(EstadoSolicitud... estados) {
        Map<String, EstadoSolicitud> mapa = new LinkedHashMap<>();
        for (EstadoSolicitud estado : estados) {
            mapa.put(estado.nombre(), estado);
        }
        return Collections.unmodifiableMap(mapa);
    }
}
