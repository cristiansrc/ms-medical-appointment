package com.medisalud.appointment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "citas")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaEntity implements Persistable<UUID> {
    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PROGRAMADA";

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private OffsetDateTime fechaCancelacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
