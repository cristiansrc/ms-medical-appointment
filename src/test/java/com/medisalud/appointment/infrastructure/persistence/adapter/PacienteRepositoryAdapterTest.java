package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.domain.model.Paciente;
import com.medisalud.appointment.infrastructure.persistence.entity.PacienteEntity;
import com.medisalud.appointment.infrastructure.persistence.mapper.PacienteMapper;
import com.medisalud.appointment.infrastructure.persistence.repository.PacienteJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteRepositoryAdapterTest {

    @Mock
    private PacienteJpaRepository jpaRepository;

    @Mock
    private PacienteMapper mapper;

    @InjectMocks
    private PacienteRepositoryAdapter adapter;

    @Test
    @DisplayName("save: debe mapear, guardar y retornar el paciente")
    void should_SaveAndReturnPaciente() {
        Paciente domain = new Paciente(UUID.randomUUID(), "Paciente Test", "12345678",
                "555-0000", "test@test.com", LocalDate.of(1990, 1, 1));
        PacienteEntity entity = new PacienteEntity();
        PacienteEntity savedEntity = new PacienteEntity();
        Paciente expected = new Paciente(domain.getId(), "Paciente Test", "12345678",
                "555-0000", "test@test.com", LocalDate.of(1990, 1, 1));

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(expected);

        Paciente result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("Paciente Test", result.getNombreCompleto());
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById: retorna paciente cuando existe")
    void should_FindById_when_Exists() {
        UUID id = UUID.randomUUID();
        PacienteEntity entity = new PacienteEntity();
        Paciente expected = new Paciente(id, "Paciente Test", "12345678",
                null, null, null);

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        Optional<Paciente> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("findById: retorna Optional.empty cuando no existe")
    void should_ReturnEmpty_when_NotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Paciente> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll: retorna lista de pacientes")
    void should_FindAll_when_Multiple() {
        PacienteEntity entity1 = new PacienteEntity();
        PacienteEntity entity2 = new PacienteEntity();
        Paciente p1 = new Paciente(UUID.randomUUID(), "Paciente Uno", "111", null, null, null);
        Paciente p2 = new Paciente(UUID.randomUUID(), "Paciente Dos", "222", null, null, null);

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(p1);
        when(mapper.toDomain(entity2)).thenReturn(p2);

        List<Paciente> result = adapter.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("existsById: retorna true/false segun corresponda")
    void should_ReturnExists_when_ById() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(true);
        assertTrue(adapter.existsById(id));

        UUID id2 = UUID.randomUUID();
        when(jpaRepository.existsById(id2)).thenReturn(false);
        assertFalse(adapter.existsById(id2));
    }

    @Test
    @DisplayName("existsByDocumentoIdentidad: retorna true/false segun corresponda")
    void should_ReturnExists_when_ByDocumento() {
        when(jpaRepository.existsByDocumentoIdentidad("12345678")).thenReturn(true);
        assertTrue(adapter.existsByDocumentoIdentidad("12345678"));

        when(jpaRepository.existsByDocumentoIdentidad("00000000")).thenReturn(false);
        assertFalse(adapter.existsByDocumentoIdentidad("00000000"));
    }
}
