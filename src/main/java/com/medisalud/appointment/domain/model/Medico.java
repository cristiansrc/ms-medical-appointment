package com.medisalud.appointment.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Medico {
    private final UUID id;
    private String nombreCompleto;
    private String especialidad;
    private String telefono;
    private String email;
    private boolean activo;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Medico(UUID id, String nombreCompleto, String especialidad, String telefono, String email) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.activo = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Medico(UUID id, String nombreCompleto, String especialidad, String telefono, String email,
                  boolean activo, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.activo = activo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void actualizar(String nombreCompleto, String especialidad, String telefono, String email) {
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.updatedAt = OffsetDateTime.now();
    }

    public void desactivar() {
        this.activo = false;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters ONLY (no setters para mantener inmutabilidad por constructor)
    public UUID getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEspecialidad() { return especialidad; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
