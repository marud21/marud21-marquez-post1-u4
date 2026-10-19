package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;

/**
 * Nivel adicional para compras INTERNACIONALES. Se ubica en la cadena antes de
 * los niveles por monto:
 * <ul>
 *   <li>Si la categoría no es INTERNACIONAL, no interviene y delega.</li>
 *   <li>Si la solicitud internacional incumple la normativa, la rechaza.</li>
 *   <li>Si cumple y el monto está dentro de su autoridad, la aprueba.</li>
 *   <li>Si cumple pero el monto la supera, la remite al nivel que corresponda por monto.</li>
 * </ul>
 */
public class RevisorCumplimientoNormativo extends NivelAprobacion {

    public static final String CATEGORIA = "INTERNACIONAL";
    public static final double LIMITE = 2_000_000;
    private static final String DOMINIO_INSTITUCIONAL = "@udes.edu.co";

    @Override
    public String getNombre() {
        return "Revisor de Cumplimiento Normativo";
    }

    @Override
    protected boolean puedeResolver(Solicitud solicitud) {
        if (!CATEGORIA.equalsIgnoreCase(solicitud.getCategoria())) {
            return false;
        }
        return !cumpleNormativa(solicitud) || solicitud.getMonto() <= LIMITE;
    }

    @Override
    protected ResultadoAprobacion decidir(Solicitud solicitud) {
        if (!cumpleNormativa(solicitud)) {
            return new ResultadoAprobacion(false, getNombre(),
                    "Compra internacional rechazada: el solicitante debe usar un correo institucional "
                            + DOMINIO_INSTITUCIONAL);
        }
        return super.decidir(solicitud);
    }

    private boolean cumpleNormativa(Solicitud solicitud) {
        String email = solicitud.getSolicitanteEmail();
        return email != null && email.toLowerCase().endsWith(DOMINIO_INSTITUCIONAL);
    }
}
