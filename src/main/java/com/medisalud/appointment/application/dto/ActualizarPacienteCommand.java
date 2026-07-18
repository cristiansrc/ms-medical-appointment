package com.medisalud.appointment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPacienteCommand {
    private String nombreCompleto;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
}
