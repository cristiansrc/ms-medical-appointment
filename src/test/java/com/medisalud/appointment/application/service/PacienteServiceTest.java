package com.medisalud.appointment.application.service;

import com.medisalud.appointment.application.port.output.PacienteRepository;
import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Paciente;
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
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    @DisplayName("Crear paciente exitosamente")
    void crearPaciente() {
        when(pacienteRepository.existsByDocumentoIdentidad(any())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));

        Paciente result = pacienteService.crear("Paciente Test", "12345678", "555-0000", "test@test.com", null);

        assertNotNull(result.getId());
        assertEquals("Paciente Test", result.getNombreCompleto());
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("Crear paciente con documento duplicado lanza BusinessException")
    void crearPacienteDuplicado() {
        when(pacienteRepository.existsByDocumentoIdentidad("12345678")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> pacienteService.crear("Paciente Test", "12345678", "555-0000", "test@test.com", null));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener paciente por ID existente")
    void obtenerPacienteExistente() {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente(id, "Paciente Test", "12345678", "555-0000",
                "test@test.com", LocalDate.of(1990, 1, 1));
        when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));

        Paciente result = pacienteService.obtenerPorId(id);

        assertEquals(id, result.getId());
        assertEquals("Paciente Test", result.getNombreCompleto());
    }

    @Test
    @DisplayName("Obtener paciente por ID inexistente lanza ResourceNotFoundException")
    void obtenerPacienteInexistente() {
        UUID id = UUID.randomUUID();
        when(pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pacienteService.obtenerPorId(id));
    }

    @Test
    @DisplayName("Listar todos los pacientes")
    void listarPacientes() {
        when(pacienteRepository.findAll()).thenReturn(List.of(
                new Paciente(UUID.randomUUID(), "Paciente Uno", "111", "555-0001", "u@test.com", null),
                new Paciente(UUID.randomUUID(), "Paciente Dos", "222", "555-0002", "d@test.com", null)
        ));

        List<Paciente> result = pacienteService.listarTodos();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Actualizar paciente exitosamente")
    void actualizarPaciente() {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente(id, "Original", "12345678", "555-0000",
                "original@test.com", LocalDate.of(1990, 1, 1));
        when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));

        Paciente result = pacienteService.actualizar(id, "Actualizado", "555-1111",
                "actualizado@test.com", LocalDate.of(1990, 6, 15));

        assertEquals("Actualizado", result.getNombreCompleto());
        assertEquals("actualizado@test.com", result.getEmail());
        verify(pacienteRepository).save(paciente);
    }

    @Test
    @DisplayName("Actualizar paciente inexistente lanza ResourceNotFoundException")
    void actualizarPacienteInexistente() {
        UUID id = UUID.randomUUID();
        when(pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> pacienteService.actualizar(id, "Test", "555-0000", "t@test.com", null));
        verify(pacienteRepository, never()).save(any());
    }
}
