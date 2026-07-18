package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.application.port.output.MedicoRepository;
import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.infrastructure.persistence.mapper.MedicoMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.MedicoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MedicoRepositoryAdapter implements MedicoRepository {

    private final MedicoJpaRepository jpaRepository;
    private final MedicoMapper mapper;

    @Override
    public Medico save(Medico medico) {
        var entity = mapper.toEntity(medico);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Medico> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Medico> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
