package com.universidad.compras.estado;

import com.universidad.compras.modelo.Solicitud;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransicionEstadoTest {

    @Test
    void ejecutarUnaSolicitudAprobadaLaDejaEjecutada() {
        Solicitud s = new Solicitud("S-030", "luis@udes.edu.co", 3000000, "MATERIAL_OFICINA", "CC-200");
        s.setEstado("APROBADA");
        ContextoSolicitud contexto = new ContextoSolicitud(s);
        contexto.ejecutar();
        assertEquals("EJECUTADA", s.getEstado());
    }

    @Test
    void ejecutarUnaSolicitudPendienteSeRechazaSinCambiarElEstado() {
        Solicitud s = new Solicitud("S-031", "ana@udes.edu.co", 1000000, "SOFTWARE", "CC-100");
        ContextoSolicitud contexto = new ContextoSolicitud(s);
        ResultadoTransicion r = contexto.ejecutar();
        assertEquals("PENDIENTE", s.getEstado());
        assertFalse(r.exitosa());
        assertEquals("Error: debe estar aprobada antes de ejecutarse", r.mensaje());
    }

    @Test
    void unaSolicitudEjecutadaNoPuedeVolverAEjecutarse() {
        Solicitud s = new Solicitud("S-032", "ana@udes.edu.co", 1000000, "SOFTWARE", "CC-100");
        s.setEstado("EJECUTADA");
        ContextoSolicitud contexto = new ContextoSolicitud(s);
        ResultadoTransicion r = contexto.ejecutar();
        assertEquals("EJECUTADA", s.getEstado());
        assertEquals("Error: ya fue ejecutada", r.mensaje());
    }

    // ---- Pruebas adicionales ----

    @Test
    void pendienteSePuedeAprobarRechazarOCancelar() {
        Solicitud a = new Solicitud("S-033", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
        Solicitud b = new Solicitud("S-034", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
        Solicitud c = new Solicitud("S-035", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");

        assertTrue(new ContextoSolicitud(a).aprobar().exitosa());
        assertTrue(new ContextoSolicitud(b).rechazar().exitosa());
        assertTrue(new ContextoSolicitud(c).cancelar().exitosa());

        assertEquals("APROBADA", a.getEstado());
        assertEquals("RECHAZADA", b.getEstado());
        assertEquals("CANCELADA", c.getEstado());
    }

    @Test
    void enAprobacionAdmiteLasMismasOperacionesQuePendiente() {
        Solicitud s = new Solicitud("S-036", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
        s.setEstado("EN_APROBACION");
        ContextoSolicitud contexto = new ContextoSolicitud(s);
        assertFalse(contexto.ejecutar().exitosa());
        assertTrue(contexto.aprobar().exitosa());
        assertEquals("APROBADA", s.getEstado());
    }

    @Test
    void unaSolicitudAprobadaNoSePuedeVolverAAprobarNiRechazar() {
        Solicitud s = new Solicitud("S-037", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
        s.setEstado("APROBADA");
        ContextoSolicitud contexto = new ContextoSolicitud(s);
        assertFalse(contexto.aprobar().exitosa());
        assertFalse(contexto.rechazar().exitosa());
        assertEquals("APROBADA", s.getEstado());
        assertTrue(contexto.cancelar().exitosa());
        assertEquals("CANCELADA", s.getEstado());
    }

    @Test
    void losEstadosFinalesRechazanTodaOperacion() {
        for (String estadoFinal : new String[]{"RECHAZADA", "CANCELADA", "EJECUTADA"}) {
            Solicitud s = new Solicitud("S-038", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
            s.setEstado(estadoFinal);
            ContextoSolicitud contexto = new ContextoSolicitud(s);
            assertFalse(contexto.aprobar().exitosa(), estadoFinal);
            assertFalse(contexto.rechazar().exitosa(), estadoFinal);
            assertFalse(contexto.ejecutar().exitosa(), estadoFinal);
            assertFalse(contexto.cancelar().exitosa(), estadoFinal);
            assertEquals(estadoFinal, s.getEstado());
        }
    }

    @Test
    void elContextoCambiaSuComportamientoAlTransicionar() {
        Solicitud s = new Solicitud("S-039", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
        ContextoSolicitud contexto = new ContextoSolicitud(s);
        assertInstanceOf(EstadoPendiente.class, contexto.getEstado());
        contexto.aprobar();
        assertSame(EstadoAprobada.INSTANCIA, contexto.getEstado());
        contexto.ejecutar();
        assertSame(EstadoEjecutada.INSTANCIA, contexto.getEstado());
    }

    @Test
    void unEstadoDesconocidoSeDetectaAlConstruirElContexto() {
        Solicitud s = new Solicitud("S-040", "ana@udes.edu.co", 1, "SOFTWARE", "CC-100");
        s.setEstado("EN_ESPERA_PROVEEDOR");
        assertThrows(IllegalArgumentException.class, () -> new ContextoSolicitud(s));
    }
}
