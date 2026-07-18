package com.medisalud.appointment.application.port.output;

import com.medisalud.appointment.domain.model.Medico;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicoRepository {
    Medico save(Medico medico);
    Optional<Medico> findById(UUID id);
    List<Medico> findAll();
    boolean existsById(UUID id);
}
