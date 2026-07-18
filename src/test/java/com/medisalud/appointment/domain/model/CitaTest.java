package com.medisalud.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CitaTest {

    @Test
    @DisplayName("Crear cita con estado PROGRAMADA")
    void crearCita() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        assertEquals("PROGRAMADA", cita.getEstado());
        assertNotNull(cita.getCreatedAt());
    }

    @Test
    @DisplayName("Cancelar cita cambia estado y registra fecha")
    void cancelarCita() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        cita.cancelar("Motivo de prueba");
        assertEquals("CANCELADA", cita.getEstado());
        assertNotNull(cita.getMotivoCancelacion());
        assertNotNull(cita.getFechaCancelacion());
    }

    @Test
    @DisplayName("Cancelar cita ya cancelada lanza excepcion")
    void cancelarCitaYaCancelada() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        cita.cancelar("Primera");
        assertThrows(IllegalStateException.class, () -> cita.cancelar("Segunda"));
    }

    @Test
    @DisplayName("Reprogramar cambia la fecha")
    void reprogramarCita() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        OffsetDateTime nuevaFecha = OffsetDateTime.now().plusDays(1);
        cita.reprogramar(nuevaFecha);
        assertEquals(nuevaFecha, cita.getFechaHora());
    }

    @Test
    @DisplayName("Reprogramar cita cancelada lanza excepcion")
    void reprogramarCancelada() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        cita.cancelar("Motivo");
        assertThrows(IllegalStateException.class, () -> cita.reprogramar(OffsetDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("estaEnFranjaPenalizable true cuando falta menos de 2h")
    void estaEnFranjaPenalizable_DentroDeRango() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                OffsetDateTime.now().plusMinutes(30));
        assertTrue(cita.estaEnFranjaPenalizable());
    }

    @Test
    @DisplayName("estaEnFranjaPenalizable false cuando falta mas de 2h")
    void estaEnFranjaPenalizable_FueraDeRango() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                OffsetDateTime.now().plusHours(3));
        assertFalse(cita.estaEnFranjaPenalizable());
    }

    @Test
    @DisplayName("Constructor completo con todos los parametros")
    void constructorCompleto() {
        UUID id = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        OffsetDateTime fechaHora = OffsetDateTime.now();
        OffsetDateTime fechaCancelacion = OffsetDateTime.now();
        OffsetDateTime createdAt = OffsetDateTime.now().minusHours(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();

        Cita cita = new Cita(id, pacienteId, medicoId, fechaHora, "CANCELADA",
                "Motivo prueba", fechaCancelacion, createdAt, updatedAt);

        assertEquals(id, cita.getId());
        assertEquals(pacienteId, cita.getPacienteId());
        assertEquals(medicoId, cita.getMedicoId());
        assertEquals(fechaHora, cita.getFechaHora());
        assertEquals("CANCELADA", cita.getEstado());
        assertEquals("Motivo prueba", cita.getMotivoCancelacion());
        assertEquals(fechaCancelacion, cita.getFechaCancelacion());
        assertEquals(createdAt, cita.getCreatedAt());
        assertEquals(updatedAt, cita.getUpdatedAt());
    }
}
