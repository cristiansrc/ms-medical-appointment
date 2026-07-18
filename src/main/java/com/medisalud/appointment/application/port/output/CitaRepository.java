package com.medisalud.appointment.application.port.output;

import com.medisalud.appointment.domain.model.Cita;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CitaRepository {
    Cita save(Cita cita);
    Optional<Cita> findById(UUID id);
    List<Cita> findByMedicoIdAndFechaBetween(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin);
    List<Cita> findByPacienteIdAndFechaBetween(UUID pacienteId, OffsetDateTime inicio, OffsetDateTime fin);
    List<Cita> findAllWithFilters(UUID medicoId, UUID pacienteId, String estado, LocalDate fechaInicio, LocalDate fechaFin);
}
