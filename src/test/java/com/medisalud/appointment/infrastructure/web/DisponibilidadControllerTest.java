package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.CitaUseCase;
import com.medisalud.appointment.domain.model.FranjaHoraria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DisponibilidadController.class)
class DisponibilidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaUseCase citaUseCase;

    @Test
    @DisplayName("GET /api/v1/disponibilidad retorna franjas disponibles")
    void should_ConsultDisponibilidad_when_ValidParams() throws Exception {
        UUID medicoId = UUID.randomUUID();
        FranjaHoraria franja = new FranjaHoraria(medicoId,
                LocalDate.of(2026, 7, 20).atTime(10, 0).atOffset(ZoneOffset.ofHours(-5)),
                LocalDate.of(2026, 7, 20).atTime(10, 30).atOffset(ZoneOffset.ofHours(-5)),
                true);
        when(citaUseCase.consultarDisponibilidad(any(), any())).thenReturn(List.of(franja));

        mockMvc.perform(get("/api/v1/disponibilidad")
                        .param("medico_id", medicoId.toString())
                        .param("fecha_inicio", "2026-07-20")
                        .param("fecha_fin", "2026-07-20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medico_id").value(medicoId.toString()))
                .andExpect(jsonPath("$.franjas").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/disponibilidad retorna franjas vacias cuando no hay disponibilidad")
    void should_ReturnEmpty_when_NoAvailability() throws Exception {
        UUID medicoId = UUID.randomUUID();
        when(citaUseCase.consultarDisponibilidad(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/disponibilidad")
                        .param("medico_id", medicoId.toString())
                        .param("fecha_inicio", "2026-07-26")
                        .param("fecha_fin", "2026-07-26")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.franjas").isEmpty());
    }
}
