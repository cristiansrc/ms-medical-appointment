package com.medisalud.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegistroPenalizacionTest {

    @Test
    @DisplayName("Crear registro penalizacion con datos validos")
    void should_CreateRegistro_when_ValidData() {
        UUID id = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID citaId = UUID.randomUUID();

        RegistroPenalizacion registro = new RegistroPenalizacion(id, pacienteId, citaId);

        assertEquals(id, registro.getId());
        assertEquals(pacienteId, registro.getPacienteId());
        assertEquals(citaId, registro.getCitaId());
        assertNotNull(registro.getFechaHora());
        assertNotNull(registro.getCreatedAt());
    }

    @Test
    @DisplayName("Registro penalizacion tiene fecha y hora al crearse")
    void should_HaveFechaHora_when_Created() {
        RegistroPenalizacion registro = new RegistroPenalizacion(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertNotNull(registro.getFechaHora());
        assertTrue(registro.getFechaHora().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Constructor completo asigna todos los campos")
    void should_CreateRegistro_when_FullConstructor() {
        UUID id = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID citaId = UUID.randomUUID();
        OffsetDateTime fechaHora = OffsetDateTime.now().minusDays(1);
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);

        RegistroPenalizacion registro = new RegistroPenalizacion(id, pacienteId, citaId, fechaHora, createdAt);

        assertEquals(id, registro.getId());
        assertEquals(pacienteId, registro.getPacienteId());
        assertEquals(citaId, registro.getCitaId());
        assertEquals(fechaHora, registro.getFechaHora());
        assertEquals(createdAt, registro.getCreatedAt());
    }
}
