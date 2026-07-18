package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.MedicoUseCase;
import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.infrastructure.web.dto.MedicoRequest;
import com.medisalud.appointment.infrastructure.web.dto.MedicoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicoController.class)
class MedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicoUseCase medicoUseCase;

    @Test
    @DisplayName("GET /api/v1/medicos retorna lista")
    void listMedicos() throws Exception {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");
        when(medicoUseCase.listarTodos()).thenReturn(List.of(medico));

        mockMvc.perform(get("/api/v1/medicos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre_completo").value("Dr. Test"));
    }

    @Test
    @DisplayName("POST /api/v1/medicos crea medico")
    void createMedico() throws Exception {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");
        when(medicoUseCase.crear(any(), any(), any(), any())).thenReturn(medico);

        mockMvc.perform(post("/api/v1/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre_completo\":\"Dr. Test\",\"especialidad\":\"Cardiologia\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }
}
