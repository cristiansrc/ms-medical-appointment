package com.medisalud.appointment.application.service;

import com.medisalud.appointment.application.port.input.PacienteUseCase;
import com.medisalud.appointment.application.port.output.PacienteRepository;
import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Paciente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService implements PacienteUseCase {

    private final PacienteRepository pacienteRepository;

    @Override
    @Transactional
    public Paciente crear(String nombreCompleto, String documentoIdentidad, String telefono, String email, LocalDate fechaNacimiento) {
        if (pacienteRepository.existsByDocumentoIdentidad(documentoIdentidad)) {
            throw new BusinessException("DUPLICATE_DOCUMENT", "Ya existe un paciente con ese documento de identidad");
        }
        Paciente paciente = new Paciente(UUID.randomUUID(), nombreCompleto, documentoIdentidad, telefono, email, fechaNacimiento);
        Paciente saved = pacienteRepository.save(paciente);
        log.info("Paciente created: {} ({})", saved.getId(), saved.getNombreCompleto());
        return saved;
    }

    @Override
    @Transactional
    public Paciente actualizar(UUID id, String nombreCompleto, String telefono, String email, LocalDate fechaNacimiento) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
        paciente.actualizar(nombreCompleto, telefono, email, fechaNacimiento);
        Paciente saved = pacienteRepository.save(paciente);
        log.info("Paciente updated: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Paciente obtenerPorId(UUID id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }
}
