package com.universidad.compras.estado;

/** Recién radicada: puede aprobarse, rechazarse o cancelarse, pero aún no ejecutarse. */
public class EstadoPendiente extends EstadoBase {

    public static final EstadoPendiente INSTANCIA = new EstadoPendiente();

    protected EstadoPendiente() {
    }

    @Override
    public String nombre() {
        return "PENDIENTE";
    }

    @Override
    public ResultadoTransicion aprobar(ContextoSolicitud contexto, String motivo) {
        return contexto.transicionarA(EstadoAprobada.INSTANCIA, motivo);
    }

    @Override
    public ResultadoTransicion rechazar(ContextoSolicitud contexto, String motivo) {
        return contexto.transicionarA(EstadoRechazada.INSTANCIA, motivo);
    }

    @Override
    public ResultadoTransicion cancelar(ContextoSolicitud contexto, String motivo) {
        return contexto.transicionarA(EstadoCancelada.INSTANCIA, motivo);
    }

    @Override
    public ResultadoTransicion ejecutar(ContextoSolicitud contexto, String motivo) {
        return ResultadoTransicion.invalida(nombre(), "Error: debe estar aprobada antes de ejecutarse");
    }
}
