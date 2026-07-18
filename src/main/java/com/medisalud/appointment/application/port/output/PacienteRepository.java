package com.medisalud.appointment.application.port.output;

import com.medisalud.appointment.domain.model.Paciente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository {
    Paciente save(Paciente paciente);
    Optional<Paciente> findById(UUID id);
    List<Paciente> findAll();
    boolean existsById(UUID id);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
