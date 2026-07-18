package com.medisalud.appointment.domain.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Validador de reglas de negocio del dominio.
 * Métodos estáticos puros, sin dependencias externas.
 */
public final class ValidadorReglasNegocio {

    private static final LocalTime HORA_INICIO = LocalTime.of(7, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);

    private ValidadorReglasNegocio() {}

    /**
     * RN-01: Solo Lunes a Sabado, 7:00-17:00, franjas de 30 min.
     */
    public static boolean esFranjaHorariaValida(OffsetDateTime fechaHora) {
        DayOfWeek dia = fechaHora.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY) return false;

        LocalTime hora = fechaHora.toLocalTime();
        return !hora.isBefore(HORA_INICIO) && !hora.isAfter(HORA_FIN.minusMinutes(30));
    }

    /**
     * RN-01b: La franja debe comenzar en hora exacta o :30 (minutos 0 o 30).
     */
    public static boolean esFranjaDe30Minutos(OffsetDateTime fechaHora) {
        int minuto = fechaHora.getMinute();
        return minuto == 0 || minuto == 30;
    }

    /**
     * RN-03: Edad minima 18 años.
     */
    public static boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return true; // si no hay fecha, no se valida
        return fechaNacimiento.plusYears(18).isBefore(LocalDate.now())
                || fechaNacimiento.plusYears(18).isEqual(LocalDate.now());
    }

    /**
     * RN-02: Verificar que medico no tenga cita en la misma franja (offset 30 min).
     */
    public static boolean medicoDisponibleEnFranja(OffsetDateTime nuevaFecha, OffsetDateTime existente) {
        long diffMinutos = Math.abs(Duration.between(nuevaFecha, existente).toMinutes());
        return diffMinutos >= 30;
    }

    /**
     * RN-04: Verificar que paciente no tenga cita en la misma franja.
     */
    public static boolean pacienteDisponibleEnFranja(OffsetDateTime nuevaFecha, OffsetDateTime existente) {
        return medicoDisponibleEnFranja(nuevaFecha, existente); // misma logica
    }

    /**
     * RN-05: Verificar si una cancelacion es tardia (≤ 2h antes).
     */
    public static boolean esCancelacionTardia(OffsetDateTime fechaHoraCita) {
        return OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).isAfter(fechaHoraCita);
    }

    /**
     * RN-05b: Verificar si el paciente tiene 3+ penalizaciones en 30 días.
     */
    public static boolean excedeLimitePenalizaciones(int penalizacionesEn30Dias) {
        return penalizacionesEn30Dias >= 3;
    }

    /**
     * RN-06: Verificar que la nueva fecha para reprogramar sea valida.
     */
    public static boolean esReprogramacionValida(OffsetDateTime nuevaFechaHora) {
        return esFranjaHorariaValida(nuevaFechaHora) && esFranjaDe30Minutos(nuevaFechaHora)
                && nuevaFechaHora.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Valida si una fecha es festivo.
     */
    public static boolean esDiaHabil(LocalDate fecha, List<LocalDate> festivos) {
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) return false;
        return festivos == null || festivos.stream().noneMatch(f -> f.equals(fecha));
    }
}
