package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.infrastructure.persistence.entity.MedicoEntity;
import com.medisalud.appointment.infrastructure.persistence.mapper.MedicoMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.MedicoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoRepositoryAdapterTest {

    @Mock
    private MedicoJpaRepository jpaRepository;

    @Mock
    private MedicoMapper mapper;

    @InjectMocks
    private MedicoRepositoryAdapter adapter;

    @Test
    @DisplayName("save: debe mapear, guardar y retornar el medico")
    void should_SaveAndReturnMedico() {
        Medico domain = new Medico(UUID.randomUUID(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");
        MedicoEntity entity = new MedicoEntity();
        MedicoEntity savedEntity = new MedicoEntity();
        Medico expected = new Medico(domain.getId(), "Dr. Test", "Cardiologia", "555-0000", "test@test.com");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(expected);

        Medico result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("Dr. Test", result.getNombreCompleto());
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById: retorna medico cuando existe")
    void should_FindById_when_Exists() {
        UUID id = UUID.randomUUID();
        MedicoEntity entity = new MedicoEntity();
        Medico expected = new Medico(id, "Dr. Test", "Cardiologia", null, null);

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        Optional<Medico> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("findById: retorna Optional.empty cuando no existe")
    void should_ReturnEmpty_when_NotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Medico> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll: retorna lista de medicos")
    void should_FindAll_when_Multiple() {
        MedicoEntity entity1 = new MedicoEntity();
        MedicoEntity entity2 = new MedicoEntity();
        Medico medico1 = new Medico(UUID.randomUUID(), "Dr. Uno", "Cardiologia", null, null);
        Medico medico2 = new Medico(UUID.randomUUID(), "Dr. Dos", "Pediatria", null, null);

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(medico1);
        when(mapper.toDomain(entity2)).thenReturn(medico2);

        List<Medico> result = adapter.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("existsById: retorna true cuando existe")
    void should_ReturnExists_when_True() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(true);

        assertTrue(adapter.existsById(id));
    }

    @Test
    @DisplayName("existsById: retorna false cuando no existe")
    void should_ReturnExists_when_False() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(false);

        assertFalse(adapter.existsById(id));
    }
}
