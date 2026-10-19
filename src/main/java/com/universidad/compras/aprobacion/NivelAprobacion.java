package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;

import java.util.List;

/**
 * Eslabón de la cadena de aprobación. Cada nivel decide, con los datos de la
 * solicitud, si está dentro de su autoridad (y entonces la aprueba o la rechaza)
 * o si debe remitirla al siguiente nivel. Ningún nivel conoce cuántos niveles
 * hay después de él ni en qué orden están: solo conoce a su sucesor inmediato.
 */
public abstract class NivelAprobacion {

    private NivelAprobacion siguiente;

    /** Nombre que se registra en {@code nivelResolutor} cuando este nivel resuelve. */
    public abstract String getNombre();

    /** ¿La solicitud está dentro de la autoridad de este nivel? */
    protected abstract boolean puedeResolver(Solicitud solicitud);

    /**
     * Decisión del nivel cuando la solicitud está dentro de su autoridad.
     * Por defecto aprueba si la solicitud es válida; cada nivel puede redefinirla.
     */
    protected ResultadoAprobacion decidir(Solicitud solicitud) {
        if (solicitud.getMonto() <= 0) {
            return new ResultadoAprobacion(false, getNombre(), "Monto inválido: debe ser mayor que cero");
        }
        if (solicitud.getCentroCosto() == null || solicitud.getCentroCosto().isBlank()) {
            return new ResultadoAprobacion(false, getNombre(), "La solicitud no indica centro de costo");
        }
        return new ResultadoAprobacion(true, getNombre(), "Aprobada por " + getNombre());
    }

    /** Punto de entrada de la cadena: resuelve aquí o delega al siguiente nivel. */
    public final ResultadoAprobacion manejar(Solicitud solicitud) {
        if (puedeResolver(solicitud)) {
            ResultadoAprobacion resultado = decidir(solicitud);
            solicitud.setNivelResolutor(getNombre());
            return resultado;
        }
        if (siguiente == null) {
            return new ResultadoAprobacion(false, getNombre(),
                    "Ningún nivel de la cadena tiene autoridad para resolver la solicitud");
        }
        return siguiente.manejar(solicitud);
    }

    /** Enlaza este nivel con su sucesor y devuelve el sucesor para encadenar llamadas. */
    public NivelAprobacion enlazarCon(NivelAprobacion siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /** Arma una cadena con los niveles en el orden recibido y devuelve el primero. */
    public static NivelAprobacion encadenar(List<? extends NivelAprobacion> niveles) {
        if (niveles == null || niveles.isEmpty()) {
            throw new IllegalArgumentException("La cadena de aprobación necesita al menos un nivel");
        }
        for (int i = 0; i < niveles.size() - 1; i++) {
            niveles.get(i).enlazarCon(niveles.get(i + 1));
        }
        return niveles.get(0);
    }
}
