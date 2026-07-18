package com.medisalud.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedicoTest {

    @Test
    @DisplayName("Crear medico con datos validos")
    void should_CreateMedico_when_ValidData() {
        UUID id = UUID.randomUUID();
        Medico medico = new Medico(id, "Dr. Test", "Cardiologia", "555-0000", "test@test.com");

        assertEquals(id, medico.getId());
        assertEquals("Dr. Test", medico.getNombreCompleto());
        assertEquals("Cardiologia", medico.getEspecialidad());
        assertEquals("555-0000", medico.getTelefono());
        assertEquals("test@test.com", medico.getEmail());
        assertTrue(medico.isActivo());
    }

    @Test
    @DisplayName("Actualizar medico cambia los campos")
    void should_ActualizarMedico_when_Called() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");

        medico.actualizar("Dr. Updated", "Dermatologia", "555-1111", "updated@test.com");

        assertEquals("Dr. Updated", medico.getNombreCompleto());
        assertEquals("Dermatologia", medico.getEspecialidad());
        assertEquals("555-1111", medico.getTelefono());
        assertEquals("updated@test.com", medico.getEmail());
    }

    @Test
    @DisplayName("Desactivar medico cambia flag activo")
    void should_DesactivarMedico_when_Called() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");

        medico.desactivar();

        assertFalse(medico.isActivo());
    }

    @Test
    @DisplayName("Medico nuevo tiene createdAt")
    void should_HaveCreatedAt_when_New() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");

        assertNotNull(medico.getCreatedAt());
        assertNotNull(medico.getUpdatedAt());
        assertTrue(medico.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Getters devuelven valores correctos")
    void should_ReturnCorrectValues_via_Getters() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Medico medico = new Medico(id, "Dr. Test", "Cardiologia", "555-0000", "test@test.com",
                true, now, now);

        assertEquals(id, medico.getId());
        assertEquals("Dr. Test", medico.getNombreCompleto());
        assertEquals("Cardiologia", medico.getEspecialidad());
        assertEquals("555-0000", medico.getTelefono());
        assertEquals("test@test.com", medico.getEmail());
        assertTrue(medico.isActivo());
        assertEquals(now, medico.getCreatedAt());
        assertEquals(now, medico.getUpdatedAt());
    }
}
