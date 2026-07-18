package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.FestivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FestivoJpaRepository extends JpaRepository<FestivoEntity, UUID> {
    List<FestivoEntity> findByYear(int year);
    Optional<FestivoEntity> findByDateAndCountryCode(LocalDate date, String countryCode);
}
