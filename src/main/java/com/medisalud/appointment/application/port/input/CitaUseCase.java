package com.medisalud.appointment.application.port.input;

import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.domain.model.FranjaHoraria;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CitaUseCase {
    Cita reservar(UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora);
    Cita cancelar(UUID citaId, String motivo);
    Cita reprogramar(UUID citaId, OffsetDateTime nuevaFechaHora);
    List<FranjaHoraria> consultarDisponibilidad(UUID medicoId, LocalDate fecha);
    Cita obtenerPorId(UUID id);
    List<Cita> listarCitas(UUID medicoId, UUID pacienteId, String estado, LocalDate fechaInicio, LocalDate fechaFin);
}
