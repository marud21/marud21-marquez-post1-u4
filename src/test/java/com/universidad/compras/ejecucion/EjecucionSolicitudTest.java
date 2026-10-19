package com.universidad.compras.ejecucion;

import com.universidad.compras.modelo.Solicitud;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EjecucionSolicitudTest {

    private final PresupuestoService presupuestoService = new PresupuestoService();
    private final OrdenCompraService ordenCompraService = new OrdenCompraService();

    @Test
    void ejecutarReservaPresupuestoYGeneraOrden() {
        Solicitud s = new Solicitud("S-010", "ana@udes.edu.co", 3000000, "SOFTWARE", "CC-100");
        s.setEstado("APROBADA");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        ejecutor.ejecutar(new ReservarPresupuestoOperacion(presupuestoService, s));
        ejecutor.ejecutar(new GenerarOrdenCompraOperacion(ordenCompraService, s, "Proveedor ACME"));
        assertEquals("EJECUTADA", s.getEstado());
    }

    @Test
    void deshacerSoloLaUltimaOperacionNoAfectaLaAnterior() {
        // ejecutar reservar presupuesto, luego generar orden; deshacer una vez
        // debe revertir solo la generación de la orden, no la reserva.
        Solicitud s = new Solicitud("S-011", "luis@udes.edu.co", 4000000, "MATERIAL_OFICINA", "CC-200");
        s.setEstado("APROBADA");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        ReservarPresupuestoOperacion reserva = new ReservarPresupuestoOperacion(presupuestoService, s);
        GenerarOrdenCompraOperacion orden = new GenerarOrdenCompraOperacion(ordenCompraService, s, "Proveedor ACME");
        assertDoesNotThrow(() -> {
            ejecutor.ejecutar(reserva);
            ejecutor.ejecutar(orden);
            ejecutor.deshacerUltima("S-011");
        });
        assertTrue(reserva.isReservado(), "La reserva de presupuesto debe seguir vigente");
        assertNull(orden.getNumeroOrden(), "La orden de compra debe quedar cancelada");
        assertEquals("APROBADA", s.getEstado(), "El undo restaura el estado anterior a la ejecución");
        assertEquals(List.of(reserva), ejecutor.operacionesActivas("S-011"));
    }

    @Test
    void elHistorialConservaTodasLasOperacionesNoSoloLaUltima() {
        Solicitud s = new Solicitud("S-012", "ana@udes.edu.co", 2000000, "SOFTWARE", "CC-100");
        s.setEstado("APROBADA");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        ejecutor.ejecutar(new ReservarPresupuestoOperacion(presupuestoService, s));
        ejecutor.ejecutar(new GenerarOrdenCompraOperacion(ordenCompraService, s, "Proveedor ACME"));
        // tras ejecutar dos operaciones sobre una misma solicitud, el historial
        // consultable debe reportar tamaño 2, no solo la última operación.
        assertDoesNotThrow(() -> {
            List<RegistroOperacion> historial = ejecutor.historial("S-012");
            assertEquals(2, historial.size());
            assertInstanceOf(ReservarPresupuestoOperacion.class, historial.get(0).operacion());
            assertInstanceOf(GenerarOrdenCompraOperacion.class, historial.get(1).operacion());
        });
    }

    // ---- Pruebas adicionales ----

    @Test
    void sePuedeLiberarElPresupuestoSinCancelarLaOrden() {
        Solicitud s = new Solicitud("S-013", "ana@udes.edu.co", 2000000, "SOFTWARE", "CC-100");
        s.setEstado("APROBADA");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        ReservarPresupuestoOperacion reserva = new ReservarPresupuestoOperacion(presupuestoService, s);
        GenerarOrdenCompraOperacion orden = new GenerarOrdenCompraOperacion(ordenCompraService, s, "Proveedor ACME");
        ejecutor.ejecutar(reserva);
        ejecutor.ejecutar(orden);

        ejecutor.deshacer("S-013", ReservarPresupuestoOperacion.class);

        assertFalse(reserva.isReservado());
        assertEquals("OC-S-013", orden.getNumeroOrden());
        assertEquals("EJECUTADA", s.getEstado());
        assertEquals(List.of(orden), ejecutor.operacionesActivas("S-013"));
    }

    @Test
    void elHistorialRegistraTambienLasOperacionesDeshechasEnOrden() {
        Solicitud s = new Solicitud("S-014", "ana@udes.edu.co", 2000000, "SOFTWARE", "CC-100");
        s.setEstado("APROBADA");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        ejecutor.ejecutar(new ReservarPresupuestoOperacion(presupuestoService, s));
        ejecutor.ejecutar(new GenerarOrdenCompraOperacion(ordenCompraService, s, "Proveedor ACME"));
        ejecutor.deshacerUltima("S-014");

        List<RegistroOperacion> historial = ejecutor.historial("S-014");
        assertEquals(3, historial.size());
        assertEquals(RegistroOperacion.Accion.DESHECHA, historial.get(2).accion());
        assertSame(historial.get(1).operacion(), historial.get(2).operacion());
    }

    @Test
    void noSeGeneraOrdenParaUnaSolicitudNoAprobada() {
        Solicitud s = new Solicitud("S-015", "ana@udes.edu.co", 2000000, "SOFTWARE", "CC-100");
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        GenerarOrdenCompraOperacion orden = new GenerarOrdenCompraOperacion(ordenCompraService, s, "Proveedor ACME");

        assertThrows(IllegalStateException.class, () -> ejecutor.ejecutar(orden));
        assertEquals("PENDIENTE", s.getEstado());
        assertTrue(ejecutor.historial("S-015").isEmpty());
    }

    @Test
    void deshacerSinOperacionesLanzaExcepcionControlada() {
        EjecutorOperaciones ejecutor = new EjecutorOperaciones();
        assertThrows(IllegalStateException.class, () -> ejecutor.deshacerUltima("S-999"));
    }
}
