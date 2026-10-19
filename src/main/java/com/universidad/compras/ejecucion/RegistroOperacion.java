package com.universidad.compras.ejecucion;

import java.time.Instant;

/** Entrada inmutable del historial de operaciones de una solicitud. */
public record RegistroOperacion(int secuencia,
                                String solicitudId,
                                OperacionSolicitud operacion,
                                Accion accion,
                                Instant momento) {

    public enum Accion { EJECUTADA, DESHECHA }

    public String getNombreOperacion() {
        return operacion.getNombre();
    }
}
