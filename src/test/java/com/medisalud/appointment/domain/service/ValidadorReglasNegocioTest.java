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
    @DisplayName("RN-06: Reprogramacion valida")
    class ReprogramacionTest {

        @Test
        @DisplayName("Reprogramacion con fecha valida")
        void esReprogramacionValida_conFechaValida() {
            OffsetDateTime fechaValida = OffsetDateTime.now().plusDays(2)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            assertTrue(ValidadorReglasNegocio.esReprogramacionValida(fechaValida));
        }

        @Test
        @DisplayName("Reprogramacion con fecha en domingo")
        void esReprogramacionValida_conDomingo() {
            // Find next Sunday
            OffsetDateTime domingo = OffsetDateTime.now().plusDays(1);
            while (domingo.getDayOfWeek() != DayOfWeek.SUNDAY) {
                domingo = domingo.plusDays(1);
            }
            domingo = domingo.withHour(10).withMinute(0).withSecond(0).withNano(0);
            assertFalse(ValidadorReglasNegocio.esReprogramacionValida(domingo));
        }

        @Test
        @DisplayName("Reprogramacion con minuto no valido (no :00 ni :30)")
        void esReprogramacionValida_conMinutoInvalido() {
            OffsetDateTime fechaInvalida = OffsetDateTime.now().plusDays(2)
                    .withHour(10).withMinute(15).withSecond(0).withNano(0);
            assertFalse(ValidadorReglasNegocio.esReprogramacionValida(fechaInvalida));
        }

        @Test
        @DisplayName("Reprogramacion con fecha pasada")
        void esReprogramacionValida_conFechaPasada() {
            OffsetDateTime fechaPasada = OffsetDateTime.now().minusDays(1)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            assertFalse(ValidadorReglasNegocio.esReprogramacionValida(fechaPasada));
        }

        @Test
        @DisplayName("Reprogramacion con hora fuera del horario permitido")
        void esReprogramacionValida_conHoraInvalida() {
            OffsetDateTime fechaFueraHorario = OffsetDateTime.now().plusDays(2)
                    .withHour(18).withMinute(0).withSecond(0).withNano(0);
            assertFalse(ValidadorReglasNegocio.esReprogramacionValida(fechaFueraHorario));
        }
    }

    @Nested
    @DisplayName("RN-02: Disponibilidad del medico en franja")
    class MedicoDisponibleTest {

        @Test
        @DisplayName("Medico disponible con mas de 30 min de diferencia")
        void medicoDisponible_conDiferenciaMayor() {
            OffsetDateTime nueva = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
            OffsetDateTime existente = OffsetDateTime.now().plusDays(1).withHour(11).withMinute(0);
            assertTrue(ValidadorReglasNegocio.medicoDisponibleEnFranja(nueva, existente));
        }

        @Test
        @DisplayName("Medico disponible con exactamente 30 min de diferencia")
        void medicoDisponible_conExactamente30Min() {
            OffsetDateTime nueva = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
            OffsetDateTime existente = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(30);
            assertTrue(ValidadorReglasNegocio.medicoDisponibleEnFranja(nueva, existente));
        }

        @Test
        @DisplayName("Medico NO disponible con menos de 30 min de diferencia")
        void medicoNoDisponible_conMenosDe30Min() {
            OffsetDateTime nueva = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
            OffsetDateTime existente = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(15);
            assertFalse(ValidadorReglasNegocio.medicoDisponibleEnFranja(nueva, existente));
        }

        @Test
        @DisplayName("Medico NO disponible con misma hora exacta")
        void medicoNoDisponible_conMismaHora() {
            OffsetDateTime mismaFecha = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
            assertFalse(ValidadorReglasNegocio.medicoDisponibleEnFranja(mismaFecha, mismaFecha));
        }

        @Test
        @DisplayName("Paciente disponible usa la misma logica que medico")
        void pacienteDisponibleEnFranja() {
            OffsetDateTime nueva = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
            OffsetDateTime existente = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(30);
            assertTrue(ValidadorReglasNegocio.pacienteDisponibleEnFranja(nueva, existente));
        }

        @Test
        @DisplayName("Paciente NO disponible con menos de 30 min")
        void pacienteNoDisponible() {
            OffsetDateTime nueva = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
            OffsetDateTime existente = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(10);
            assertFalse(ValidadorReglasNegocio.pacienteDisponibleEnFranja(nueva, existente));
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
