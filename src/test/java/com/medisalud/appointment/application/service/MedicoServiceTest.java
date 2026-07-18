package com.medisalud.appointment.application.service;

import com.medisalud.appointment.application.port.output.MedicoRepository;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Medico;
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
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoService medicoService;

    @Test
    @DisplayName("Crear medico exitosamente")
    void crearMedico() {
        when(medicoRepository.save(any(Medico.class))).thenAnswer(i -> i.getArgument(0));

        Medico result = medicoService.crear("Dr. Test", "Cardiologia", "555-0000", "test@test.com");

        assertNotNull(result.getId());
        assertEquals("Dr. Test", result.getNombreCompleto());
        verify(medicoRepository).save(any(Medico.class));
    }

    @Test
    @DisplayName("Obtener medico por ID existente")
    void obtenerMedicoExistente() {
        UUID id = UUID.randomUUID();
        Medico medico = new Medico(id, "Dr. Test", "Cardiologia", "555-0000", "test@test.com");
        when(medicoRepository.findById(id)).thenReturn(Optional.of(medico));

        Medico result = medicoService.obtenerPorId(id);

        assertEquals(id, result.getId());
        assertEquals("Dr. Test", result.getNombreCompleto());
    }

    @Test
    @DisplayName("Obtener medico por ID inexistente lanza ResourceNotFoundException")
    void obtenerMedicoInexistente() {
        UUID id = UUID.randomUUID();
        when(medicoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicoService.obtenerPorId(id));
    }

    @Test
    @DisplayName("Listar todos los medicos")
    void listarMedicos() {
        when(medicoRepository.findAll()).thenReturn(List.of(
                new Medico(UUID.randomUUID(), "Dr. Uno", "Cardiologia", null, null),
                new Medico(UUID.randomUUID(), "Dr. Dos", "Pediatria", null, null)
        ));

        List<Medico> result = medicoService.listarTodos();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Eliminar (soft-delete) medico existente")
    void eliminarMedico() {
        UUID id = UUID.randomUUID();
        Medico medico = new Medico(id, "Dr. Test", "Cardiologia", null, null);
        when(medicoRepository.findById(id)).thenReturn(Optional.of(medico));
        when(medicoRepository.save(any(Medico.class))).thenAnswer(i -> i.getArgument(0));

        medicoService.eliminar(id);

        assertFalse(medico.isActivo());
        verify(medicoRepository).save(medico);
    }
}
