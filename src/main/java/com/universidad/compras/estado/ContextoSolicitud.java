package com.universidad.compras.estado;

import com.universidad.compras.modelo.Solicitud;
import com.universidad.compras.notificacion.EventoCambioEstado;
import com.universidad.compras.notificacion.PublicadorCambiosEstado;

/**
 * Contexto del patrón State: envuelve una {@link Solicitud} y delega cada
 * operación (aprobar, rechazar, ejecutar, cancelar) en el objeto que representa
 * su estado actual. Es el único punto donde el estado de una solicitud cambia,
 * y por eso es también donde se publica el evento para los suscriptores.
 */
public class ContextoSolicitud {

    private final Solicitud solicitud;
    private final PublicadorCambiosEstado publicador;
    private EstadoSolicitud estado;

    public ContextoSolicitud(Solicitud solicitud) {
        this(solicitud, new PublicadorCambiosEstado());
    }

    public ContextoSolicitud(Solicitud solicitud, PublicadorCambiosEstado publicador) {
        this.solicitud = solicitud;
        this.publicador = publicador;
        this.estado = CatalogoEstados.obtener(solicitud.getEstado());
    }

    public ResultadoTransicion aprobar() {
        return aprobar("Solicitud aprobada");
    }

    public ResultadoTransicion aprobar(String motivo) {
        return estadoActual().aprobar(this, motivo);
    }

    public ResultadoTransicion rechazar() {
        return rechazar("Solicitud rechazada");
    }

    public ResultadoTransicion rechazar(String motivo) {
        return estadoActual().rechazar(this, motivo);
    }

    public ResultadoTransicion ejecutar() {
        return ejecutar("Solicitud ejecutada");
    }

    public ResultadoTransicion ejecutar(String motivo) {
        return estadoActual().ejecutar(this, motivo);
    }

    public ResultadoTransicion cancelar() {
        return cancelar("Solicitud cancelada");
    }

    public ResultadoTransicion cancelar(String motivo) {
        return estadoActual().cancelar(this, motivo);
    }

    /**
     * Compensación usada exclusivamente por el undo de la Necesidad 2: devuelve
     * la solicitud a un estado previo registrado por la operación que se deshace.
     * No es una operación de negocio, por eso no pasa por las reglas de transición,
     * pero sí notifica a los suscriptores.
     */
    public ResultadoTransicion revertirA(String nombreEstado, String motivo) {
        return transicionarA(CatalogoEstados.obtener(nombreEstado), motivo);
    }

    public EstadoSolicitud getEstado() {
        return estadoActual();
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    /** Solo los objetos estado (mismo paquete) deciden cuándo transicionar. */
    ResultadoTransicion transicionarA(EstadoSolicitud nuevo, String motivo) {
        String anterior = solicitud.getEstado();
        estado = nuevo;
        solicitud.setEstado(nuevo.nombre());
        publicador.publicar(EventoCambioEstado.de(solicitud, anterior, motivo));
        return ResultadoTransicion.exitosa(nuevo.nombre(), motivo);
    }

    /**
     * Si otro contexto (u otro código) cambió el String de la solicitud, el
     * contexto se resincroniza antes de operar para no decidir con un estado viejo.
     */
    private EstadoSolicitud estadoActual() {
        if (!estado.nombre().equals(solicitud.getEstado())) {
            estado = CatalogoEstados.obtener(solicitud.getEstado());
        }
        return estado;
    }
}
