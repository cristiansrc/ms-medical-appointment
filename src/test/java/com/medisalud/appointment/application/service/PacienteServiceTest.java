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
}
