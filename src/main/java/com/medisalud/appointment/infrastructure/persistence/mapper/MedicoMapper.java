package com.medisalud.appointment.infrastructure.persistence.mapper;

import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.infrastructure.persistence.entity.MedicoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicoMapper {

    default Medico toDomain(MedicoEntity entity) {
        if (entity == null) return null;
        return new Medico(
                entity.getId(),
                entity.getNombreCompleto(),
                entity.getEspecialidad(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.isActivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    MedicoEntity toEntity(Medico domain);
}
