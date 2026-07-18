package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, UUID> {
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    Optional<PacienteEntity> findByDocumentoIdentidad(String documentoIdentidad);
}
