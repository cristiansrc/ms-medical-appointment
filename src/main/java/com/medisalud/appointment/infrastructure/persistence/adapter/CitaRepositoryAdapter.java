package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.application.port.output.CitaRepository;
import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.infrastructure.persistence.mapper.CitaMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.CitaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CitaRepositoryAdapter implements CitaRepository {

    private final CitaJpaRepository jpaRepository;
    private final CitaMapper mapper;

    @Override
    public Cita save(Cita cita) {
        var entity = mapper.toEntity(cita);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Cita> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Cita> findByMedicoIdAndFechaBetween(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin) {
        return jpaRepository.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraDesc(medicoId, inicio, fin)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Cita> findByPacienteIdAndFechaBetween(UUID pacienteId, OffsetDateTime inicio, OffsetDateTime fin) {
        return jpaRepository.findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(pacienteId, inicio, fin)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Cita> findAllWithFilters(UUID medicoId, UUID pacienteId, String estado, LocalDate fechaInicio, LocalDate fechaFin) {
        return jpaRepository.findAllWithFilters(medicoId, pacienteId, estado, fechaInicio, fechaFin)
                .stream().map(mapper::toDomain).toList();
    }
}
