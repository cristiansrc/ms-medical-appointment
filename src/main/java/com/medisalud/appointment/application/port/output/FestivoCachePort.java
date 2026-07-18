package com.medisalud.appointment.application.port.output;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de salida para el cache de festivos.
 * Separado de FestivoRepository para mantener la capa de aplicacion limpia
 * de dependencias de infraestructura.
 */
public interface FestivoCachePort {
    List<LocalDate> cargarFestivosSiEsNecesario(int anio, String pais);
    boolean esFestivo(LocalDate fecha);
}
