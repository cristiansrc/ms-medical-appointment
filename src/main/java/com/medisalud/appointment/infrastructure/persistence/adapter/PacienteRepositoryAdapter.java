package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.application.port.output.PacienteRepository;
import com.medisalud.appointment.domain.model.Paciente;
import com.medisalud.appointment.infrastructure.persistence.mapper.PacienteMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.PacienteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PacienteRepositoryAdapter implements PacienteRepository {

    private final PacienteJpaRepository jpaRepository;
    private final PacienteMapper mapper;

    @Override
    public Paciente save(Paciente paciente) {
        var entity = mapper.toEntity(paciente);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Paciente> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Paciente> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
        return jpaRepository.existsByDocumentoIdentidad(documentoIdentidad);
    }
}
