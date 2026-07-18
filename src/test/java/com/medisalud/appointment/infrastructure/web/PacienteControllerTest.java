package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.PacienteUseCase;
import com.medisalud.appointment.domain.model.Paciente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteUseCase pacienteUseCase;

    @Test
    @DisplayName("POST /api/v1/pacientes crea paciente exitosamente")
    void should_CreatePaciente_when_ValidRequest() throws Exception {
        Paciente paciente = new Paciente(UUID.randomUUID(), "Paciente Test", "12345678",
                "555-0000", "test@test.com", LocalDate.of(1990, 1, 1));
        when(pacienteUseCase.crear(any(), any(), any(), any(), any())).thenReturn(paciente);

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre_completo\":\"Paciente Test\",\"documento_identidad\":\"12345678\","
                                + "\"telefono\":\"555-0000\",\"email\":\"test@test.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("GET /api/v1/pacientes/{id} retorna paciente cuando existe")
    void should_GetPaciente_when_Exists() throws Exception {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente(id, "Paciente Test", "12345678",
                "555-0000", "test@test.com", LocalDate.of(1990, 1, 1));
        when(pacienteUseCase.obtenerPorId(id)).thenReturn(paciente);

        mockMvc.perform(get("/api/v1/pacientes/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_completo").value("Paciente Test"));
    }

    @Test
    @DisplayName("GET /api/v1/pacientes retorna lista de pacientes")
    void should_ListPacientes_when_Any() throws Exception {
        Paciente paciente = new Paciente(UUID.randomUUID(), "Paciente Test", "12345678",
                "555-0000", "test@test.com", LocalDate.of(1990, 1, 1));
        when(pacienteUseCase.listarTodos()).thenReturn(List.of(paciente));

        mockMvc.perform(get("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre_completo").value("Paciente Test"));
    }

    @Test
    @DisplayName("PUT /api/v1/pacientes/{id} actualiza paciente exitosamente")
    void should_UpdatePaciente_when_ValidRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente(id, "Paciente Actualizado", "12345678",
                "555-0000", "actualizado@test.com", LocalDate.of(1990, 1, 1));
        when(pacienteUseCase.actualizar(any(), any(), any(), any(), any())).thenReturn(paciente);

        mockMvc.perform(put("/api/v1/pacientes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre_completo\":\"Paciente Actualizado\",\"documento_identidad\":\"12345678\","
                                + "\"telefono\":\"555-0000\",\"email\":\"actualizado@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_completo").value("Paciente Actualizado"));
    }
}
