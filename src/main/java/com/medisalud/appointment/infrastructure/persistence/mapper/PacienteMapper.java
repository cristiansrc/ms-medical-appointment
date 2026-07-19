package com.medisalud.appointment.infrastructure.persistence.mapper;

import com.medisalud.appointment.domain.model.Paciente;
import com.medisalud.appointment.infrastructure.persistence.entity.PacienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PacienteMapper {

    default Paciente toDomain(PacienteEntity entity) {
        if (entity == null) return null;
        return new Paciente(
                entity.getId(),
                entity.getNombreCompleto(),
                entity.getDocumentoIdentidad(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getBirthDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Mapping(target = "birthDate", source = "fechaNacimiento")
    @Mapping(target = "isNew", ignore = true)
    PacienteEntity toEntity(Paciente domain);
}
