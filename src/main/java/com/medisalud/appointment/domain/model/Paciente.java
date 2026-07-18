package com.medisalud.appointment.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Paciente {
    private final UUID id;
    private String nombreCompleto;
    private String documentoIdentidad;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Paciente(UUID id, String nombreCompleto, String documentoIdentidad, String telefono,
                    String email, LocalDate fechaNacimiento) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.documentoIdentidad = documentoIdentidad;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Paciente(UUID id, String nombreCompleto, String documentoIdentidad, String telefono,
                    String email, LocalDate fechaNacimiento, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.documentoIdentidad = documentoIdentidad;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void actualizar(String nombreCompleto, String telefono, String email, LocalDate fechaNacimiento) {
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
