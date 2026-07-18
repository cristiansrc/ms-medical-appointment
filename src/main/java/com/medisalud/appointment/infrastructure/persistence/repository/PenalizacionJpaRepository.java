package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.PenalizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface PenalizacionJpaRepository extends JpaRepository<PenalizacionEntity, UUID> {

    int countByPacienteIdAndFechaHoraAfter(UUID pacienteId, OffsetDateTime fechaHora);
}
