package com.medisalud.appointment.infrastructure.persistence.mapper;

import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.infrastructure.persistence.entity.CitaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CitaMapper {

    default Cita toDomain(CitaEntity entity) {
        if (entity == null) return null;
        return new Cita(
                entity.getId(),
                entity.getPacienteId(),
                entity.getMedicoId(),
                entity.getFechaHora(),
                entity.getEstado(),
                entity.getMotivoCancelacion(),
                entity.getFechaCancelacion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    CitaEntity toEntity(Cita domain);
}
