package com.medisalud.appointment.application.port.input;

import com.medisalud.appointment.domain.model.Paciente;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PacienteUseCase {
    Paciente crear(String nombreCompleto, String documentoIdentidad, String telefono, String email, LocalDate fechaNacimiento);
    Paciente actualizar(UUID id, String nombreCompleto, String telefono, String email, LocalDate fechaNacimiento);
    Paciente obtenerPorId(UUID id);
    List<Paciente> listarTodos();
}
