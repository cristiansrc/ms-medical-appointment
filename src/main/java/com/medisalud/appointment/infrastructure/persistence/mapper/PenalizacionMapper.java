package com.medisalud.appointment.infrastructure.persistence.mapper;

import com.medisalud.appointment.domain.model.RegistroPenalizacion;
import com.medisalud.appointment.infrastructure.persistence.entity.PenalizacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PenalizacionMapper {

    default RegistroPenalizacion toDomain(PenalizacionEntity entity) {
        if (entity == null) return null;
        return new RegistroPenalizacion(
                entity.getId(),
                entity.getPacienteId(),
                entity.getCitaId(),
                entity.getFechaHora(),
                entity.getCreatedAt()
        );
    }

    @Mapping(target = "isNew", ignore = true)
    PenalizacionEntity toEntity(RegistroPenalizacion domain);
}
