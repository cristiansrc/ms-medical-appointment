package com.medisalud.appointment.infrastructure.persistence.mapper;

import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.infrastructure.persistence.entity.CitaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Mapping(target = "isNew", ignore = true)
    CitaEntity toEntity(Cita domain);
}
