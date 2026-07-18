package com.medisalud.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PacienteTest {

    @Test
    @DisplayName("Crear paciente con datos validos")
    void should_CreatePaciente_when_ValidData() {
        UUID id = UUID.randomUUID();
        LocalDate nacimiento = LocalDate.of(1990, 5, 15);
        Paciente paciente = new Paciente(id, "Paciente Test", "DNI12345678", "555-0000",
                "paciente@test.com", nacimiento);

        assertEquals(id, paciente.getId());
        assertEquals("Paciente Test", paciente.getNombreCompleto());
        assertEquals("DNI12345678", paciente.getDocumentoIdentidad());
        assertEquals("555-0000", paciente.getTelefono());
        assertEquals("paciente@test.com", paciente.getEmail());
        assertEquals(nacimiento, paciente.getFechaNacimiento());
        assertNotNull(paciente.getCreatedAt());
        assertNotNull(paciente.getUpdatedAt());
    }

    @Test
    @DisplayName("Actualizar paciente cambia los campos")
    void should_ActualizarPaciente_when_Called() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "Paciente Test", "DNI12345678",
                "555-0000", "paciente@test.com", LocalDate.of(1990, 5, 15));

        LocalDate nuevaFecha = LocalDate.of(1991, 6, 20);
        paciente.actualizar("Paciente Updated", "555-1111", "updated@test.com", nuevaFecha);

        assertEquals("Paciente Updated", paciente.getNombreCompleto());
        assertEquals("555-1111", paciente.getTelefono());
        assertEquals("updated@test.com", paciente.getEmail());
        assertEquals(nuevaFecha, paciente.getFechaNacimiento());
    }

    @Test
    @DisplayName("Paciente tiene documento de identidad")
    void should_HaveDocumentoIdentidad_when_Created() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "Paciente Test", "DNI12345678",
                "555-0000", "paciente@test.com", LocalDate.of(1990, 5, 15));

        assertEquals("DNI12345678", paciente.getDocumentoIdentidad());
    }

    @Test
    @DisplayName("Getters devuelven valores correctos")
    void should_ReturnCorrectValues_via_Getters() {
        UUID id = UUID.randomUUID();
        LocalDate nacimiento = LocalDate.of(1990, 5, 15);
        OffsetDateTime now = OffsetDateTime.now();
        Paciente paciente = new Paciente(id, "Paciente Test", "DNI12345678", "555-0000",
                "paciente@test.com", nacimiento, now, now);

        assertEquals(id, paciente.getId());
        assertEquals("Paciente Test", paciente.getNombreCompleto());
        assertEquals("DNI12345678", paciente.getDocumentoIdentidad());
        assertEquals("555-0000", paciente.getTelefono());
        assertEquals("paciente@test.com", paciente.getEmail());
        assertEquals(nacimiento, paciente.getFechaNacimiento());
        assertEquals(now, paciente.getCreatedAt());
        assertEquals(now, paciente.getUpdatedAt());
    }
}
