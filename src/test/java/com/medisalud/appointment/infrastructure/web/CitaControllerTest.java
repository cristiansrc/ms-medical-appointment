package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.CitaUseCase;
import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.infrastructure.web.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CitaController.class, GlobalExceptionHandler.class})
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaUseCase citaUseCase;

    @Test
    @DisplayName("POST /api/v1/citas crea cita exitosamente")
    void createCita() throws Exception {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        when(citaUseCase.reservar(any(), any(), any())).thenReturn(cita);

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paciente_id\":\"" + cita.getPacienteId()
                                + "\",\"medico_id\":\"" + cita.getMedicoId()
                                + "\",\"fecha_hora\":\"2026-07-20T10:00:00-05:00\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/citas con regla de negocio retorna 422")
    void createCitaBusinessError() throws Exception {
        when(citaUseCase.reservar(any(), any(), any()))
                .thenThrow(new BusinessException("INVALID_SCHEDULE", "Las citas solo estan disponibles de Lunes a Sabado"));

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paciente_id\":\"" + UUID.randomUUID()
                                + "\",\"medico_id\":\"" + UUID.randomUUID()
                                + "\",\"fecha_hora\":\"2026-07-26T10:00:00-05:00\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_SCHEDULE"));
    }

    @Test
    @DisplayName("GET /api/v1/citas/{id} con cita inexistente retorna 404")
    void getCitaNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(citaUseCase.obtenerPorId(id))
                .thenThrow(new ResourceNotFoundException("Cita", id));

        mockMvc.perform(get("/api/v1/citas/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
