package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.application.port.output.PenalizacionRepository;
import com.medisalud.appointment.domain.model.RegistroPenalizacion;
import com.medisalud.appointment.infrastructure.persistence.mapper.PenalizacionMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.PenalizacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PenalizacionRepositoryAdapter implements PenalizacionRepository {

    private final PenalizacionJpaRepository jpaRepository;
    private final PenalizacionMapper mapper;

    @Override
    public RegistroPenalizacion save(RegistroPenalizacion penalizacion) {
        var entity = mapper.toEntity(penalizacion);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public int countByPacienteIdAndFechaAfter(UUID pacienteId, OffsetDateTime fecha) {
        return jpaRepository.countByPacienteIdAndFechaHoraAfter(pacienteId, fecha);
    }
}
