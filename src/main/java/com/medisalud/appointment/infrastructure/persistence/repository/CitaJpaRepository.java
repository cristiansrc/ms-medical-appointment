package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.CitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CitaJpaRepository extends JpaRepository<CitaEntity, UUID> {

    List<CitaEntity> findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraDesc(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin);

    List<CitaEntity> findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(UUID pacienteId, OffsetDateTime inicio, OffsetDateTime fin);

    @Query("SELECT c FROM CitaEntity c WHERE " +
           "(:medicoId IS NULL OR c.medicoId = :medicoId) AND " +
           "(:pacienteId IS NULL OR c.pacienteId = :pacienteId) AND " +
           "(:estado IS NULL OR c.estado = :estado) AND " +
           "(:fechaInicio IS NULL OR CAST(c.fechaHora AS LocalDate) >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR CAST(c.fechaHora AS LocalDate) <= :fechaFin) " +
           "ORDER BY c.fechaHora DESC")
    List<CitaEntity> findAllWithFilters(@Param("medicoId") UUID medicoId,
                                         @Param("pacienteId") UUID pacienteId,
                                         @Param("estado") String estado,
                                         @Param("fechaInicio") LocalDate fechaInicio,
                                         @Param("fechaFin") LocalDate fechaFin);
}
