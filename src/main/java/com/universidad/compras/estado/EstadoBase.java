package com.universidad.compras.estado;

/**
 * Por defecto todas las operaciones son inválidas: cada estado concreto
 * redefine únicamente las que sí admite. Así un estado nuevo no puede dejar
 * una combinación "sin cubrir": lo que no declara, lo rechaza.
 */
public abstract class EstadoBase implements EstadoSolicitud {

    @Override
    public ResultadoTransicion aprobar(ContextoSolicitud contexto, String motivo) {
        return operacionInvalida("aprobar");
    }

    @Override
    public ResultadoTransicion rechazar(ContextoSolicitud contexto, String motivo) {
        return operacionInvalida("rechazar");
    }

    @Override
    public ResultadoTransicion ejecutar(ContextoSolicitud contexto, String motivo) {
        return operacionInvalida("ejecutar");
    }

    @Override
    public ResultadoTransicion cancelar(ContextoSolicitud contexto, String motivo) {
        return operacionInvalida("cancelar");
    }

    protected ResultadoTransicion operacionInvalida(String operacion) {
        return ResultadoTransicion.invalida(nombre(),
                "Error: no se puede " + operacion + " una solicitud " + nombre());
    }

    @Override
    public String toString() {
        return nombre();
    }
}
