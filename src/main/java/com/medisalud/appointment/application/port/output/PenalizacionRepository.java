package com.medisalud.appointment.application.port.output;

import com.medisalud.appointment.domain.model.RegistroPenalizacion;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PenalizacionRepository {
    RegistroPenalizacion save(RegistroPenalizacion penalizacion);
    int countByPacienteIdAndFechaAfter(UUID pacienteId, OffsetDateTime fecha);
}
