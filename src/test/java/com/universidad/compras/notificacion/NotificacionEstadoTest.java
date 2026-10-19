package com.universidad.compras.notificacion;

import com.universidad.compras.aprobacion.CadenasAprobacion;
import com.universidad.compras.aprobacion.ServicioAprobacionPorNiveles;
import com.universidad.compras.ejecucion.EjecutorOperaciones;
import com.universidad.compras.ejecucion.GenerarOrdenCompraOperacion;
import com.universidad.compras.ejecucion.OrdenCompraService;
import com.universidad.compras.estado.ContextoSolicitud;
import com.universidad.compras.modelo.Solicitud;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionEstadoTest {

    /** Cuarto suscriptor de prueba: solo guarda los eventos que recibe. */
    static class ColectorEventos implements ObservadorCambioEstado {
        final List<EventoCambioEstado> eventos = new ArrayList<>();

        @Override
        public void alCambiarEstado(EventoCambioEstado evento) {
            eventos.add(evento);
        }
    }

    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();

    @BeforeEach
    void capturarSalida() {
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(salidaOriginal);
    }

    private String salidaCapturada() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void cambiarEstadoDisparaLasTresReaccionesSinLanzarExcepcion() {
        Solicitud s = new Solicitud("S-020", "ana@udes.edu.co", 2500000, "SOFTWARE", "CC-100");
        PublicadorCambiosEstado mecanismo = PublicadorCambiosEstado.conReaccionesEstandar();
        assertDoesNotThrow(() -> new ContextoSolicitud(s, mecanismo).aprobar());

        String log = salidaCapturada();
        assertTrue(log.contains("[EMAIL -> ana@udes.edu.co]"), log);
        assertTrue(log.contains("[DASHBOARD] Solicitud S-020 -> APROBADA"), log);
        assertTrue(log.contains("[AUDITORIA] S-020 -> APROBADA (PENDIENTE -> APROBADA"), log);
    }

    @Test
    void agregarUnCuartoSuscriptorDePruebaNoRequiereModificarElMecanismo() {
        // registrar un colector de prueba adicional (sin tocar la clase central
        // del mecanismo) y verificar que también reacciona al cambio de estado.
        Solicitud s = new Solicitud("S-021", "luis@udes.edu.co", 1200000, "SOFTWARE", "CC-200");
        PublicadorCambiosEstado mecanismo = PublicadorCambiosEstado.conReaccionesEstandar();
        ColectorEventos colector = new ColectorEventos();
        assertDoesNotThrow(() -> {
            mecanismo.suscribir(colector);
            new ContextoSolicitud(s, mecanismo).cancelar("Duplicada");
        });
        assertEquals(4, mecanismo.cantidadSuscriptores());
        assertEquals(1, colector.eventos.size());
        EventoCambioEstado evento = colector.eventos.get(0);
        assertEquals("PENDIENTE", evento.estadoAnterior());
        assertEquals("CANCELADA", evento.estadoNuevo());
        assertEquals("Duplicada", evento.detalle());
    }

    // ---- Integración con los puntos de cambio de estado de la Parte 1 ----

    @Test
    void laResolucionDeLaCadenaDeAprobacionNotifica() {
        PublicadorCambiosEstado publicador = PublicadorCambiosEstado.conReaccionesEstandar();
        ColectorEventos colector = new ColectorEventos();
        publicador.suscribir(colector);
        ServicioAprobacionPorNiveles servicio =
                new ServicioAprobacionPorNiveles(CadenasAprobacion.conCumplimientoNormativo(), publicador);

        servicio.evaluar(new Solicitud("S-022", "ana@udes.edu.co", 6000000, "SOFTWARE", "CC-100"));

        assertEquals(1, colector.eventos.size());
        assertEquals("APROBADA", colector.eventos.get(0).estadoNuevo());
        assertTrue(salidaCapturada().contains("[EMAIL -> ana@udes.edu.co] Solicitud S-022 APROBADA"));
    }

    @Test
    void ejecutarYCancelarLaOrdenDeCompraNotificaAmbosCambios() {
        PublicadorCambiosEstado publicador = PublicadorCambiosEstado.conReaccionesEstandar();
        ColectorEventos colector = new ColectorEventos();
        publicador.suscribir(colector);
        Solicitud s = new Solicitud("S-023", "ana@udes.edu.co", 3000000, "SOFTWARE", "CC-100");
        s.setEstado("APROBADA");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();

        ejecutor.ejecutar(new GenerarOrdenCompraOperacion(new OrdenCompraService(), s, "Proveedor ACME", publicador));
        ejecutor.deshacerUltima("S-023");

        assertEquals(2, colector.eventos.size());
        assertEquals("EJECUTADA", colector.eventos.get(0).estadoNuevo());
        assertEquals("APROBADA", colector.eventos.get(1).estadoNuevo());
        assertTrue(colector.eventos.get(1).detalle().contains("cancelada"));
    }

    @Test
    void unaOperacionInvalidaNoNotifica() {
        PublicadorCambiosEstado publicador = new PublicadorCambiosEstado();
        ColectorEventos colector = new ColectorEventos();
        publicador.suscribir(colector);
        Solicitud s = new Solicitud("S-024", "ana@udes.edu.co", 1000000, "SOFTWARE", "CC-100");

        new ContextoSolicitud(s, publicador).ejecutar();

        assertTrue(colector.eventos.isEmpty());
    }

    @Test
    void unSuscriptorQueFallaNoImpideQueLosDemasReaccionen() {
        PublicadorCambiosEstado publicador = new PublicadorCambiosEstado();
        ColectorEventos colector = new ColectorEventos();
        publicador.suscribir(evento -> { throw new IllegalStateException("canal caído"); });
        publicador.suscribir(colector);
        Solicitud s = new Solicitud("S-025", "ana@udes.edu.co", 1000000, "SOFTWARE", "CC-100");

        assertDoesNotThrow(() -> new ContextoSolicitud(s, publicador).aprobar());
        assertEquals(1, colector.eventos.size());
    }
}
