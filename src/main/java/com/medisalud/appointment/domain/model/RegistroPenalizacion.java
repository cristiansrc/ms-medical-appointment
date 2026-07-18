package com.medisalud.appointment.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RegistroPenalizacion {
    private final UUID id;
    private final UUID pacienteId;
    private final UUID citaId;
    private final OffsetDateTime fechaHora;
    private final OffsetDateTime createdAt;

    public RegistroPenalizacion(UUID id, UUID pacienteId, UUID citaId) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.citaId = citaId;
        this.fechaHora = OffsetDateTime.now();
        this.createdAt = OffsetDateTime.now();
    }

    public RegistroPenalizacion(UUID id, UUID pacienteId, UUID citaId, OffsetDateTime fechaHora, OffsetDateTime createdAt) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.citaId = citaId;
        this.fechaHora = fechaHora;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getCitaId() { return citaId; }
    public OffsetDateTime getFechaHora() { return fechaHora; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
