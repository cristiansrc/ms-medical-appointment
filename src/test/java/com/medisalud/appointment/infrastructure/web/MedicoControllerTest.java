package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.MedicoUseCase;
import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.infrastructure.web.dto.MedicoRequest;
import com.medisalud.appointment.infrastructure.web.dto.MedicoResponse;
import com.medisalud.appointment.infrastructure.web.dto.MedicoUpdateRequest;
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
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @DisplayName("GET /api/v1/medicos/{id} retorna medico")
    void should_GetMedico_when_Exists() throws Exception {
        UUID id = UUID.randomUUID();
        Medico medico = new Medico(id, "Dr. Test", "Cardiologia", "555-0000", "test@test.com");
        when(medicoUseCase.obtenerPorId(id)).thenReturn(medico);

        mockMvc.perform(get("/api/v1/medicos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_completo").value("Dr. Test"))
                .andExpect(jsonPath("$.especialidad").value("Cardiologia"));
    }

    @Test
    @DisplayName("PUT /api/v1/medicos/{id} actualiza medico")
    void should_UpdateMedico_when_ValidRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Medico medicoActualizado = new Medico(id, "Dra. Updated", "Dermatologia", "555-1111", "updated@test.com");
        when(medicoUseCase.actualizar(eq(id), any(), any(), any(), any())).thenReturn(medicoActualizado);

        mockMvc.perform(put("/api/v1/medicos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre_completo\":\"Dra. Updated\",\"especialidad\":\"Dermatologia\",\"telefono\":\"555-1111\",\"email\":\"updated@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_completo").value("Dra. Updated"))
                .andExpect(jsonPath("$.especialidad").value("Dermatologia"))
                .andExpect(jsonPath("$.telefono").value("555-1111"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }
}
