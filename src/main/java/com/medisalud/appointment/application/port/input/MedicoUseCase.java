package com.medisalud.appointment.application.port.input;

import com.medisalud.appointment.domain.model.Medico;
import java.util.List;
import java.util.UUID;

public interface MedicoUseCase {
    Medico crear(String nombreCompleto, String especialidad, String telefono, String email);
    Medico actualizar(UUID id, String nombreCompleto, String especialidad, String telefono, String email);
    Medico obtenerPorId(UUID id);
    List<Medico> listarTodos();
    void eliminar(UUID id); // soft delete (desactivar)
}
