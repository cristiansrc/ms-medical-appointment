package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.CitaUseCase;
import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ConflictException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Cita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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
                .thenThrow(new BusinessException("INVALID_SLOT", "Las citas solo estan disponibles de Lunes a Sabado"));

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paciente_id\":\"" + UUID.randomUUID()
                                + "\",\"medico_id\":\"" + UUID.randomUUID()
                                + "\",\"fecha_hora\":\"2026-07-26T10:00:00-05:00\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_SLOT"));
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

    @Test
    @DisplayName("DELETE /api/v1/citas/{id} cancela cita exitosamente")
    void should_CancelCita_when_Exists() throws Exception {
        UUID citaId = UUID.randomUUID();
        Cita cita = new Cita(citaId, UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now().plusDays(1));
        when(citaUseCase.cancelar(any(), any())).thenReturn(cita);

        mockMvc.perform(delete("/api/v1/citas/" + citaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/citas/{id}/reprogramar reprograma cita exitosamente")
    void should_ReprogramarCita_when_Valid() throws Exception {
        UUID citaId = UUID.randomUUID();
        Cita cita = new Cita(citaId, UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now().plusDays(2));
        when(citaUseCase.reprogramar(any(), any())).thenReturn(cita);

        mockMvc.perform(post("/api/v1/citas/" + citaId + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha_hora\":\"2026-07-22T10:00:00-05:00\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/citas/{id} con cita inexistente retorna 404")
    void should_Return404_when_CitaNotFoundOnCancel() throws Exception {
        UUID citaId = UUID.randomUUID();
        when(citaUseCase.cancelar(any(), any()))
                .thenThrow(new ResourceNotFoundException("Cita", citaId));

        mockMvc.perform(delete("/api/v1/citas/" + citaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/v1/citas/{id} con cita ya cancelada retorna 422")
    void should_Return422_when_AlreadyCancelled() throws Exception {
        UUID citaId = UUID.randomUUID();
        when(citaUseCase.cancelar(any(), any()))
                .thenThrow(new BusinessException("CITA_ALREADY_CANCELLED", "La cita ya fue cancelada"));

        mockMvc.perform(delete("/api/v1/citas/" + citaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CITA_ALREADY_CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/v1/citas con conflicto de horario retorna 409")
    void should_Return409_when_ConflictException() throws Exception {
        when(citaUseCase.reservar(any(), any(), any()))
                .thenThrow(new ConflictException("MEDICO_SLOT_CONFLICT", "Medico ocupado"));

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paciente_id\":\"" + UUID.randomUUID()
                                + "\",\"medico_id\":\"" + UUID.randomUUID()
                                + "\",\"fecha_hora\":\"2026-07-20T10:00:00-05:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEDICO_SLOT_CONFLICT"));
    }

    @Test
    @DisplayName("GET /api/v1/citas retorna lista de citas")
    void should_ListCitas() throws Exception {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        when(citaUseCase.listarCitas(any(), any(), any(), any(), any())).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("Listar citas sin filtro de estado retorna 200")
    void should_ListCitas_when_EstadoNull() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        when(citaUseCase.listarCitas(any(), any(), isNull(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/citas")
                        .param("medico_id", medicoId.toString())
                        .param("paciente_id", pacienteId.toString())
                        .param("fecha_inicio", "2026-07-20")
                        .param("fecha_fin", "2026-07-25")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/citas/{id} retorna cita cuando existe")
    void should_GetCita_when_Exists() throws Exception {
        UUID citaId = UUID.randomUUID();
        Cita cita = new Cita(citaId, UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        when(citaUseCase.obtenerPorId(citaId)).thenReturn(cita);

        mockMvc.perform(get("/api/v1/citas/" + citaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaId.toString()));
    }
}
