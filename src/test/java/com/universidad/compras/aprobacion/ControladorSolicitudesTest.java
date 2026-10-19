package com.universidad.compras.aprobacion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica el endpoint POST /api/solicitudes/evaluar con la cadena real del contexto Spring. */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorSolicitudesTest {

    @Autowired
    private MockMvc mockMvc;

    private String json(String id, String email, double monto, String categoria, String centroCosto) {
        return """
                {"id":"%s","solicitanteEmail":"%s","monto":%s,"categoria":"%s","centroCosto":"%s"}
                """.formatted(id, email, monto, categoria, centroCosto);
    }

    @Test
    void evaluarSolicitudDelGerenteResponde200() throws Exception {
        mockMvc.perform(post("/api/solicitudes/evaluar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("S-100", "luis@udes.edu.co", 6000000, "SOFTWARE", "CC-200")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprobada").value(true))
                .andExpect(jsonPath("$.nivelResolutor").value("Gerente de Área"));
    }

    @Test
    void evaluarSolicitudInternacionalPasaPorCumplimiento() throws Exception {
        mockMvc.perform(post("/api/solicitudes/evaluar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("S-101", "gerencia@udes.edu.co", 1000000, "INTERNACIONAL", "CC-300")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelResolutor").value("Revisor de Cumplimiento Normativo"));
    }

    @Test
    void solicitudRechazadaResponde422() throws Exception {
        mockMvc.perform(post("/api/solicitudes/evaluar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("S-102", "externo@gmail.com", 1000000, "INTERNACIONAL", "CC-300")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.aprobada").value(false));
    }
}
