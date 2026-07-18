package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.domain.model.RegistroPenalizacion;
import com.medisalud.appointment.infrastructure.persistence.entity.PenalizacionEntity;
import com.medisalud.appointment.infrastructure.persistence.mapper.PenalizacionMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.PenalizacionJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenalizacionRepositoryAdapterTest {

    @Mock
    private PenalizacionJpaRepository jpaRepository;

    @Mock
    private PenalizacionMapper mapper;

    @InjectMocks
    private PenalizacionRepositoryAdapter adapter;

    @Test
    @DisplayName("save: debe mapear, guardar y retornar la penalizacion")
    void should_SaveAndReturnPenalizacion() {
        RegistroPenalizacion domain = new RegistroPenalizacion(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        PenalizacionEntity entity = new PenalizacionEntity();
        PenalizacionEntity savedEntity = new PenalizacionEntity();
        RegistroPenalizacion expected = new RegistroPenalizacion(
                domain.getId(), domain.getPacienteId(), domain.getCitaId());

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(expected);

        RegistroPenalizacion result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(domain.getId(), result.getId());
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("countByPacienteIdAndFechaAfter: retorna el conteo correcto")
    void should_CountByPacienteIdAndFechaAfter() {
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.now().minusDays(30);

        when(jpaRepository.countByPacienteIdAndFechaHoraAfter(pacienteId, fecha)).thenReturn(3);

        int count = adapter.countByPacienteIdAndFechaAfter(pacienteId, fecha);

        assertEquals(3, count);
    }

    @Test
    @DisplayName("countByPacienteIdAndFechaAfter: retorna 0 cuando no hay penalizaciones")
    void should_ReturnZero_when_NoPenalizaciones() {
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.now().minusDays(30);

        when(jpaRepository.countByPacienteIdAndFechaHoraAfter(pacienteId, fecha)).thenReturn(0);

        int count = adapter.countByPacienteIdAndFechaAfter(pacienteId, fecha);

        assertEquals(0, count);
    }
}
