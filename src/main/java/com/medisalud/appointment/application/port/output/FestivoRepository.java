package com.medisalud.appointment.application.port.output;

import java.time.LocalDate;
import java.util.List;

public interface FestivoRepository {
    List<LocalDate> obtenerFestivos(int anio, String pais);
    boolean esFestivo(LocalDate fecha);
}
