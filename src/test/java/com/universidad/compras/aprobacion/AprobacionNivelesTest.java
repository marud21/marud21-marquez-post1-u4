package com.universidad.compras.aprobacion;

import com.universidad.compras.modelo.Solicitud;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AprobacionNivelesTest {

    @Test
    void solicitudDentroDeAutoridadDelSupervisorSeAprueba() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.estandar());
        Solicitud s = new Solicitud("S-001", "ana@udes.edu.co", 1500000, "MATERIAL_OFICINA", "CC-100");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertTrue(r.isAprobada());
        assertEquals("Supervisor de Área", r.getNivelResolutor());
        assertEquals("Supervisor de Área", s.getNivelResolutor());
        assertEquals("APROBADA", s.getEstado());
    }

    @Test
    void solicitudQueSuperaAlSupervisorEscalaAlGerente() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.estandar());
        Solicitud s = new Solicitud("S-002", "luis@udes.edu.co", 6000000, "SOFTWARE", "CC-200");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertTrue(r.isAprobada());
        assertEquals("Gerente de Área", r.getNivelResolutor());
    }

    @Test
    void solicitudInternacionalPasaPorCumplimientoAntesDelNivelPorMonto() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.conCumplimientoNormativo());
        Solicitud s = new Solicitud("S-003", "gerencia@udes.edu.co", 1000000, "INTERNACIONAL", "CC-300");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertEquals("Revisor de Cumplimiento Normativo", r.getNivelResolutor());
    }

    // ---- Pruebas adicionales ----

    @Test
    void solicitudSobreDiezMillonesLlegaAlDirectorFinanciero() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.conCumplimientoNormativo());
        Solicitud s = new Solicitud("S-004", "rectoria@udes.edu.co", 45000000, "SOFTWARE", "CC-400");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertTrue(r.isAprobada());
        assertEquals("Director Financiero", r.getNivelResolutor());
    }

    @Test
    void solicitudNoInternacionalAtraviesaElRevisorSinQueEsteIntervenga() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.conCumplimientoNormativo());
        Solicitud s = new Solicitud("S-005", "ana@udes.edu.co", 1500000, "MATERIAL_OFICINA", "CC-100");
        assertEquals("Supervisor de Área", servicio.evaluar(s).getNivelResolutor());
    }

    @Test
    void internacionalQueCumplePeroSuperaAlRevisorSigueAlNivelPorMonto() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.conCumplimientoNormativo());
        Solicitud s = new Solicitud("S-006", "gerencia@udes.edu.co", 6000000, "INTERNACIONAL", "CC-300");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertTrue(r.isAprobada());
        assertEquals("Gerente de Área", r.getNivelResolutor());
    }

    @Test
    void internacionalQueIncumpleNormativaEsRechazadaPorElRevisor() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.conCumplimientoNormativo());
        Solicitud s = new Solicitud("S-007", "proveedor@gmail.com", 6000000, "INTERNACIONAL", "CC-300");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertFalse(r.isAprobada());
        assertEquals("Revisor de Cumplimiento Normativo", r.getNivelResolutor());
        assertEquals("RECHAZADA", s.getEstado());
    }

    @Test
    void elNivelDentroDeSuAutoridadPuedeRechazar() {
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(CadenasAprobacion.estandar());
        Solicitud s = new Solicitud("S-008", "ana@udes.edu.co", 500000, "MATERIAL_OFICINA", "");
        ResultadoAprobacion r = servicio.evaluar(s);
        assertFalse(r.isAprobada());
        assertEquals("Supervisor de Área", r.getNivelResolutor());
    }

    @Test
    void reordenarOQuitarNivelesNoRequiereModificarLosNiveles() {
        // Cadena sin Supervisor: el Gerente resuelve lo que antes resolvía el Supervisor.
        NivelAprobacion sinSupervisor = NivelAprobacion.encadenar(List.of(new GerenteArea(), new DirectorFinanciero()));
        ServicioAprobacion servicio = new ServicioAprobacionPorNiveles(sinSupervisor);
        Solicitud s = new Solicitud("S-009", "ana@udes.edu.co", 1500000, "MATERIAL_OFICINA", "CC-100");
        assertEquals("Gerente de Área", servicio.evaluar(s).getNivelResolutor());
    }
}
