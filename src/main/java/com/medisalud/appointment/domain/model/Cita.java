package com.medisalud.appointment.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Cita {
    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private OffsetDateTime fechaHora;
    private String estado;
    private String motivoCancelacion;
    private OffsetDateTime fechaCancelacion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Cita(UUID id, UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fechaHora = fechaHora;
        this.estado = "PROGRAMADA";
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Cita(UUID id, UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora,
                String estado, String motivoCancelacion, OffsetDateTime fechaCancelacion,
                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
        this.fechaCancelacion = fechaCancelacion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void cancelar(String motivo) {
        if (!"PROGRAMADA".equals(this.estado)) {
            throw new IllegalStateException("Solo se pueden cancelar citas en estado PROGRAMADA");
        }
        this.estado = "CANCELADA";
        this.motivoCancelacion = motivo;
        this.fechaCancelacion = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void reprogramar(OffsetDateTime nuevaFechaHora) {
        if (!"PROGRAMADA".equals(this.estado)) {
            throw new IllegalStateException("Solo se pueden reprogramar citas en estado PROGRAMADA");
        }
        this.fechaHora = nuevaFechaHora;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public OffsetDateTime getFechaHora() { return fechaHora; }
    public String getEstado() { return estado; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public OffsetDateTime getFechaCancelacion() { return fechaCancelacion; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public boolean estaEnFranjaPenalizable() {
        OffsetDateTime now = OffsetDateTime.now();
        return fechaHora.isAfter(now) && fechaHora.isBefore(now.plusHours(2));
    }
}
