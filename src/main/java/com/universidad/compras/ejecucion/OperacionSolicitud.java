package com.universidad.compras.ejecucion;

/**
 * Operación discreta y reversible sobre una solicitud aprobada. Encapsula al
 * receptor (servicio de bajo nivel) y los datos necesarios para ejecutarla y
 * para deshacerla después, de forma independiente de las demás operaciones.
 */
public interface OperacionSolicitud {

    String getSolicitudId();

    /** Nombre legible para el historial. */
    String getNombre();

    void ejecutar();

    void deshacer();
}
