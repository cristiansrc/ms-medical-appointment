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

    private static final ZoneOffset COLOMBIA_OFFSET = ZoneOffset.ofHours(-5);

    private ValidadorReglasNegocio() {}

    /**
     * RN-01: Lun-Vie 08:00-18:00 (ultima franja 17:30), Sab 08:00-13:00 (ultima franja 12:30), Dom/festivos sin atencion.
     * Normaliza a hora Colombia (UTC-5) independientemente del offset que envie el cliente.
     */
    public static boolean esFranjaHorariaValida(OffsetDateTime fechaHora) {
        OffsetDateTime colombiaTime = fechaHora.withOffsetSameInstant(COLOMBIA_OFFSET);
        DayOfWeek dia = colombiaTime.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY) return false;

        LocalTime hora = colombiaTime.toLocalTime();
        if (dia == DayOfWeek.SATURDAY) {
            // Sabado: 08:00 - 13:00 (ultima franja 12:30)
            return !hora.isBefore(LocalTime.of(8, 0)) && !hora.isAfter(LocalTime.of(12, 30));
        }
        // Lunes a Viernes: 08:00 - 18:00 (ultima franja 17:30)
        return !hora.isBefore(LocalTime.of(8, 0)) && !hora.isAfter(LocalTime.of(17, 30));
    }

    public static ZoneOffset getColombiaOffset() {
        return COLOMBIA_OFFSET;
    }

    /**
     * RN-01b: La franja debe comenzar en hora exacta o :30 (minutos 0 o 30).
     */
    public static boolean esFranjaDe30Minutos(OffsetDateTime fechaHora) {
        int minuto = fechaHora.getMinute();
        return minuto == 0 || minuto == 30;
    }

    /**
     * RN-03: No se aceptan fechas de nacimiento futuras. Si no se proporciona, se omite validacion.
     */
    public static boolean esFechaNacimientoValida(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return true;
        return !fechaNacimiento.isAfter(LocalDate.now());
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
