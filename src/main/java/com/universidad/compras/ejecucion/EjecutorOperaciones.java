package com.universidad.compras.ejecucion;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Invocador de las operaciones de ejecución. Por cada solicitud mantiene:
 * <ul>
 *   <li>una pila de operaciones activas (ejecutadas y aún no deshechas), para el undo;</li>
 *   <li>un historial ordenado de todo lo ocurrido (ejecuciones y deshacer), consultable.</li>
 * </ul>
 * No conoce PresupuestoService ni OrdenCompraService: solo trabaja con {@link OperacionSolicitud}.
 */
public class EjecutorOperaciones {

    private final Map<String, Deque<OperacionSolicitud>> activas = new HashMap<>();
    private final Map<String, List<RegistroOperacion>> historial = new HashMap<>();

    public synchronized void ejecutar(OperacionSolicitud operacion) {
        operacion.ejecutar();
        activas.computeIfAbsent(operacion.getSolicitudId(), id -> new ArrayDeque<>()).push(operacion);
        registrar(operacion, RegistroOperacion.Accion.EJECUTADA);
    }

    /** Deshace la operación más reciente aún activa de la solicitud. */
    public synchronized OperacionSolicitud deshacerUltima(String solicitudId) {
        Deque<OperacionSolicitud> pila = activas.get(solicitudId);
        if (pila == null || pila.isEmpty()) {
            throw new IllegalStateException("No hay operaciones por deshacer para " + solicitudId);
        }
        OperacionSolicitud operacion = pila.peek();
        operacion.deshacer();
        pila.pop();
        registrar(operacion, RegistroOperacion.Accion.DESHECHA);
        return operacion;
    }

    /**
     * Deshace la operación activa más reciente del tipo indicado, sin afectar las
     * demás (por ejemplo, liberar el presupuesto sin cancelar la orden ya generada).
     */
    public synchronized <T extends OperacionSolicitud> T deshacer(String solicitudId, Class<T> tipo) {
        Deque<OperacionSolicitud> pila = activas.getOrDefault(solicitudId, new ArrayDeque<>());
        Iterator<OperacionSolicitud> it = pila.iterator();
        while (it.hasNext()) {
            OperacionSolicitud operacion = it.next();
            if (tipo.isInstance(operacion)) {
                operacion.deshacer();
                it.remove();
                registrar(operacion, RegistroOperacion.Accion.DESHECHA);
                return tipo.cast(operacion);
            }
        }
        throw new IllegalStateException("No hay una operación activa de tipo " + tipo.getSimpleName()
                + " para " + solicitudId);
    }

    /** Historial completo y ordenado (de la más antigua a la más reciente). */
    public synchronized List<RegistroOperacion> historial(String solicitudId) {
        return List.copyOf(historial.getOrDefault(solicitudId, List.of()));
    }

    /** Operaciones ejecutadas y todavía no deshechas, en orden cronológico. */
    public synchronized List<OperacionSolicitud> operacionesActivas(String solicitudId) {
        List<OperacionSolicitud> resultado = new ArrayList<>();
        activas.getOrDefault(solicitudId, new ArrayDeque<>()).descendingIterator().forEachRemaining(resultado::add);
        return List.copyOf(resultado);
    }

    private void registrar(OperacionSolicitud operacion, RegistroOperacion.Accion accion) {
        List<RegistroOperacion> registros = historial.computeIfAbsent(operacion.getSolicitudId(), id -> new ArrayList<>());
        registros.add(new RegistroOperacion(registros.size() + 1, operacion.getSolicitudId(),
                operacion, accion, Instant.now()));
    }
}
