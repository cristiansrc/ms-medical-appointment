package com.medisalud.appointment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservarCitaCommand {
    private UUID pacienteId;
    private UUID medicoId;
    private OffsetDateTime fechaHora;
}
