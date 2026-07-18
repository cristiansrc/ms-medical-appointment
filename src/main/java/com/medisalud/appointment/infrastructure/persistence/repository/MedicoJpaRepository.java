package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.MedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, UUID> {
}
