package com.medisalud.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FranjaHorariaTest {

    @Test
    @DisplayName("Crear franja horaria con datos validos")
    void should_CreateFranja_when_ValidData() {
        UUID medicoId = UUID.randomUUID();
        OffsetDateTime inicio = OffsetDateTime.now();
        OffsetDateTime fin = inicio.plusHours(1);

        FranjaHoraria franja = new FranjaHoraria(medicoId, inicio, fin, true);

        assertEquals(medicoId, franja.getMedicoId());
        assertEquals(inicio, franja.getInicio());
        assertEquals(fin, franja.getFin());
        assertTrue(franja.isDisponible());
    }

    @Test
    @DisplayName("Dos franjas mismo medico e inicio son iguales")
    void should_BeEqual_when_SameMedicoAndInicio() {
        UUID medicoId = UUID.randomUUID();
        OffsetDateTime inicio = OffsetDateTime.now();
        OffsetDateTime fin = inicio.plusHours(1);

        FranjaHoraria franja1 = new FranjaHoraria(medicoId, inicio, fin, true);
        FranjaHoraria franja2 = new FranjaHoraria(medicoId, inicio, fin.plusHours(2), false);

        assertEquals(franja1, franja2);
        assertEquals(franja1.hashCode(), franja2.hashCode());
    }

    @Test
    @DisplayName("Dos franjas distinto medico no son iguales")
    void should_NotBeEqual_when_DifferentMedico() {
        OffsetDateTime inicio = OffsetDateTime.now();
        FranjaHoraria franja1 = new FranjaHoraria(UUID.randomUUID(), inicio, inicio.plusHours(1), true);
        FranjaHoraria franja2 = new FranjaHoraria(UUID.randomUUID(), inicio, inicio.plusHours(1), true);

        assertNotEquals(franja1, franja2);
    }

    @Test
    @DisplayName("Franja disponible cuando flag es true")
    void should_BeDisponible_when_FlagTrue() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1), true);

        assertTrue(franja.isDisponible());
    }

    @Test
    @DisplayName("Franja no disponible cuando flag es false")
    void should_NotBeDisponible_when_FlagFalse() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1), false);

        assertFalse(franja.isDisponible());
    }

    @Test
    @DisplayName("equals con mismo objeto retorna true")
    void should_BeEqual_when_SameObject() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1), true);

        //noinspection EqualsWithItself
        assertTrue(franja.equals(franja));
    }

    @Test
    @DisplayName("equals con null retorna false")
    void should_NotBeEqual_when_Null() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1), true);

        assertNotNull(franja);
    }
}
