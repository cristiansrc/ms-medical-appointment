package com.medisalud.appointment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "festivos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivoEntity implements Persistable<UUID> {
    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "local_name", nullable = false, length = 255)
    private String localName;

    @Column(length = 255)
    private String name;

    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;

    private Boolean fixed;

    @Column(name = "\"global\"")
    private Boolean global;

    @Column(columnDefinition = "TEXT")
    private String types;

    @Column(nullable = false)
    private Integer year;

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
