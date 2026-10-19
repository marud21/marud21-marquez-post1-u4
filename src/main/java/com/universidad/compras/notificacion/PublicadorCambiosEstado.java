package com.universidad.compras.notificacion;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sujeto observable. El código que cambia el estado de una solicitud solo llama
 * a {@link #publicar}; no sabe quién está suscrito ni cuántos son. Agregar una
 * reacción nueva es suscribir un observador más, sin modificar esta clase.
 */
public class PublicadorCambiosEstado {

    private final List<ObservadorCambioEstado> observadores = new CopyOnWriteArrayList<>();

    public PublicadorCambiosEstado() {
    }

    public PublicadorCambiosEstado(List<? extends ObservadorCambioEstado> iniciales) {
        observadores.addAll(iniciales);
    }

    /** Publicador con las tres reacciones pedidas por Compras: correo, tablero y auditoría. */
    public static PublicadorCambiosEstado conReaccionesEstandar() {
        return new PublicadorCambiosEstado(List.of(
                new NotificadorCorreoSolicitante(),
                new ActualizadorDashboardContabilidad(),
                new RegistradorAuditoria()));
    }

    public void suscribir(ObservadorCambioEstado observador) {
        observadores.add(observador);
    }

    public void desuscribir(ObservadorCambioEstado observador) {
        observadores.remove(observador);
    }

    public int cantidadSuscriptores() {
        return observadores.size();
    }

    /**
     * Notifica a todos los suscriptores. Un suscriptor que falla no impide que
     * los demás reciban el evento.
     */
    public void publicar(EventoCambioEstado evento) {
        for (ObservadorCambioEstado observador : observadores) {
            try {
                observador.alCambiarEstado(evento);
            } catch (RuntimeException e) {
                System.err.println("[NOTIFICACION] Falló " + observador.getClass().getSimpleName()
                        + " para " + evento.solicitudId() + ": " + e.getMessage());
            }
        }
    }
}
