package com.medisalud.appointment.application.service;

import com.medisalud.appointment.application.port.input.MedicoUseCase;
import com.medisalud.appointment.application.port.output.MedicoRepository;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Medico;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicoService implements MedicoUseCase {

    private final MedicoRepository medicoRepository;

    private String sanitizarTelefono(String telefono) {
        if (telefono == null) return null;
        return telefono.replaceAll("\\D", "");
    }

    @Override
    @Transactional
    public Medico crear(String nombreCompleto, String especialidad, String telefono, String email) {
        Medico medico = new Medico(UUID.randomUUID(), nombreCompleto, especialidad, sanitizarTelefono(telefono), email);
        Medico saved = medicoRepository.save(medico);
        log.info("Medico created: {} ({})", saved.getId(), saved.getNombreCompleto());
        return saved;
    }

    @Override
    @Transactional
    public Medico actualizar(UUID id, String nombreCompleto, String especialidad, String telefono, String email) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medico", id));
        medico.actualizar(nombreCompleto, especialidad, sanitizarTelefono(telefono), email);
        Medico saved = medicoRepository.save(medico);
        log.info("Medico updated: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Medico obtenerPorId(UUID id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medico", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medico", id));
        medico.desactivar();
        medicoRepository.save(medico);
        log.info("Medico soft-deleted: {}", id);
    }
}
