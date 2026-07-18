package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.infrastructure.persistence.entity.CitaEntity;
import com.medisalud.appointment.infrastructure.persistence.mapper.CitaMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.CitaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaRepositoryAdapterTest {

    @Mock
    private CitaJpaRepository jpaRepository;

    @Mock
    private CitaMapper mapper;

    @InjectMocks
    private CitaRepositoryAdapter adapter;

    @Test
    @DisplayName("save: debe mapear, guardar y retornar la cita")
    void should_SaveAndReturnCita() {
        UUID id = UUID.randomUUID();
        Cita domain = new Cita(id, UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
        CitaEntity entity = new CitaEntity();
        CitaEntity savedEntity = new CitaEntity();
        Cita expected = new Cita(id, domain.getPacienteId(), domain.getMedicoId(), domain.getFechaHora());

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(expected);

        Cita result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById: retorna cita cuando existe")
    void should_FindById_when_Exists() {
        UUID id = UUID.randomUUID();
        CitaEntity entity = new CitaEntity();
        Cita expected = new Cita(id, UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        Optional<Cita> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("findById: retorna Optional.empty cuando no existe")
    void should_ReturnEmpty_when_NotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Cita> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByMedicoIdAndFechaBetween: retorna citas del medico")
    void should_FindByMedicoIdAndFechaBetween() {
        UUID medicoId = UUID.randomUUID();
        OffsetDateTime inicio = OffsetDateTime.now().minusHours(1);
        OffsetDateTime fin = OffsetDateTime.now().plusHours(1);
        CitaEntity entity = new CitaEntity();
        Cita expected = new Cita(UUID.randomUUID(), UUID.randomUUID(), medicoId, OffsetDateTime.now());

        when(jpaRepository.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraDesc(medicoId, inicio, fin))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        List<Cita> result = adapter.findByMedicoIdAndFechaBetween(medicoId, inicio, fin);

        assertEquals(1, result.size());
        assertEquals(medicoId, result.get(0).getMedicoId());
    }

    @Test
    @DisplayName("findAllWithFilters: retorna citas filtradas")
    void should_FindAllWithFilters() {
        UUID medicoId = UUID.randomUUID();
        CitaEntity entity = new CitaEntity();
        Cita expected = new Cita(UUID.randomUUID(), UUID.randomUUID(), medicoId, OffsetDateTime.now());

        when(jpaRepository.findAllWithFilters(eq(medicoId), isNull(), eq("PROGRAMADA"), any(), any()))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        List<Cita> result = adapter.findAllWithFilters(medicoId, null, "PROGRAMADA", LocalDate.now(), LocalDate.now());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByPacienteIdAndFechaBetween: retorna citas del paciente")
    void should_FindByPacienteIdAndFechaBetween() {
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime inicio = OffsetDateTime.now().minusHours(1);
        OffsetDateTime fin = OffsetDateTime.now().plusHours(1);
        CitaEntity entity = new CitaEntity();
        entity.setId(UUID.randomUUID());
        entity.setPacienteId(pacienteId);
        entity.setMedicoId(UUID.randomUUID());
        entity.setFechaHora(OffsetDateTime.now());
        entity.setEstado("PROGRAMADA");
        Cita expected = new Cita(entity.getId(), pacienteId, entity.getMedicoId(), entity.getFechaHora());

        when(jpaRepository.findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(pacienteId, inicio, fin))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        List<Cita> result = adapter.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);

        assertEquals(1, result.size());
        assertEquals(pacienteId, result.get(0).getPacienteId());
        verify(jpaRepository).findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(pacienteId, inicio, fin);
    }
}
