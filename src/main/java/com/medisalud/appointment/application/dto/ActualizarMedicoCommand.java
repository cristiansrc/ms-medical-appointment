package com.medisalud.appointment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarMedicoCommand {
    private String nombreCompleto;
    private String especialidad;
    private String telefono;
    private String email;
}
