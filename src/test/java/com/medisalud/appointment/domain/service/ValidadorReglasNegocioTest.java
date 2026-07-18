package com.medisalud.appointment.domain.service;

import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.domain.model.EstadoCita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorReglasNegocioTest {

    @Nested
    @DisplayName("RN-01: Franja horaria valida")
    class FranjaHorariaTest {

        @ParameterizedTest
        @CsvSource({
            "2026-07-20, 07:00, true",   // Lunes 7:00
            "2026-07-20, 16:30, true",   // Lunes 16:30
            "2026-07-25, 10:00, true",   // Sabado 10:00
            "2026-07-26, 10:00, false",  // Domingo
            "2026-07-20, 06:59, false",  // Antes de 7:00
            "2026-07-20, 17:01, false",  // Despues de 17:00
        })
        void testEsFranjaHorariaValida(String fecha, String hora, boolean esperado) {
            OffsetDateTime fechaHora = LocalDate.parse(fecha)
                    .atTime(LocalTime.parse(hora))
                    .atOffset(ZoneOffset.ofHours(-5));
            assertEquals(esperado, ValidadorReglasNegocio.esFranjaHorariaValida(fechaHora));
        }
    }

    @Nested
    @DisplayName("RN-01b: Franja de 30 minutos")
    class Franja30MinTest {

        @ParameterizedTest
        @CsvSource({
            "2026-07-20T07:00:00-05:00, true",
            "2026-07-20T07:30:00-05:00, true",
            "2026-07-20T07:15:00-05:00, false",
            "2026-07-20T07:45:00-05:00, false",
        })
        void testEsFranjaDe30Minutos(String fechaStr, boolean esperado) {
            OffsetDateTime fechaHora = OffsetDateTime.parse(fechaStr);
            assertEquals(esperado, ValidadorReglasNegocio.esFranjaDe30Minutos(fechaHora));
        }
    }

    @Nested
    @DisplayName("RN-03: Edad minima 18 años")
    class EdadMinimaTest {

        @Test
        @DisplayName("Paciente mayor de 18 años")
        void mayorDeEdad() {
            LocalDate fecha = LocalDate.now().minusYears(20);
            assertTrue(ValidadorReglasNegocio.esMayorDeEdad(fecha));
        }

        @Test
        @DisplayName("Paciente menor de 18 años")
        void menorDeEdad() {
            LocalDate fecha = LocalDate.now().minusYears(17);
            assertFalse(ValidadorReglasNegocio.esMayorDeEdad(fecha));
        }

        @Test
        @DisplayName("Paciente exactamente 18 años")
        void exactamente18() {
            LocalDate fecha = LocalDate.now().minusYears(18);
            assertTrue(ValidadorReglasNegocio.esMayorDeEdad(fecha));
        }

        @Test
        @DisplayName("Fecha nula no bloquea")
        void fechaNula() {
            assertTrue(ValidadorReglasNegocio.esMayorDeEdad(null));
        }
    }

    @Nested
    @DisplayName("RN-05: Cancelacion tardia y penalizaciones")
    class PenalizacionTest {

        @Test
        @DisplayName("Cancelacion con menos de 2h de anticipacion es tardia")
        void cancelacionTardia() {
            OffsetDateTime citaPronto = OffsetDateTime.now().plusHours(1);
            assertTrue(ValidadorReglasNegocio.esCancelacionTardia(citaPronto));
        }

        @Test
        @DisplayName("Cancelacion con mas de 2h de anticipacion no es tardia")
        void cancelacionNoTardia() {
            OffsetDateTime citaLejana = OffsetDateTime.now().plusHours(3);
            assertFalse(ValidadorReglasNegocio.esCancelacionTardia(citaLejana));
        }

        @Test
        @DisplayName("3 penalizaciones excede el limite")
        void excedeLimite() {
            assertTrue(ValidadorReglasNegocio.excedeLimitePenalizaciones(3));
        }

        @Test
        @DisplayName("2 penalizaciones no excede el limite")
        void noExcedeLimite() {
            assertFalse(ValidadorReglasNegocio.excedeLimitePenalizaciones(2));
        }
    }

    @Nested
    @DisplayName("Dias festivos y habiles")
    class FestivoTest {

        @Test
        @DisplayName("Domingo no es dia habil")
        void domingoNoHabil() {
            LocalDate domingo = LocalDate.of(2026, 7, 26); // domingo
            assertFalse(ValidadorReglasNegocio.esDiaHabil(domingo, List.of()));
        }

        @Test
        @DisplayName("Festivo no es dia habil")
        void festivoNoHabil() {
            LocalDate lunes = LocalDate.of(2026, 7, 20);
            assertFalse(ValidadorReglasNegocio.esDiaHabil(lunes, List.of(lunes)));
        }

        @Test
        @DisplayName("Lunes normal es dia habil")
        void lunesHabil() {
            LocalDate lunes = LocalDate.of(2026, 7, 20);
            assertTrue(ValidadorReglasNegocio.esDiaHabil(lunes, List.of()));
        }
    }
}
