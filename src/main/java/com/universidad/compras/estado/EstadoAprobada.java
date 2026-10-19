package com.universidad.compras.estado;

/** Aprobada: puede ejecutarse o cancelarse; no puede volver a aprobarse ni rechazarse. */
public class EstadoAprobada extends EstadoBase {

    public static final EstadoAprobada INSTANCIA = new EstadoAprobada();

    private EstadoAprobada() {
    }

    @Override
    public String nombre() {
        return "APROBADA";
    }

    @Override
    public ResultadoTransicion ejecutar(ContextoSolicitud contexto, String motivo) {
        return contexto.transicionarA(EstadoEjecutada.INSTANCIA, motivo);
    }

    @Override
    public ResultadoTransicion cancelar(ContextoSolicitud contexto, String motivo) {
        return contexto.transicionarA(EstadoCancelada.INSTANCIA, motivo);
    }

    @Override
    public ResultadoTransicion aprobar(ContextoSolicitud contexto, String motivo) {
        return ResultadoTransicion.invalida(nombre(), "Error: ya fue aprobada");
    }
}
