package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.MedicoUseCase;
import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.infrastructure.web.api.MedicosApi;
import com.medisalud.appointment.infrastructure.web.dto.MedicoRequest;
import com.medisalud.appointment.infrastructure.web.dto.MedicoResponse;
import com.medisalud.appointment.infrastructure.web.dto.MedicoUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MedicoController implements MedicosApi {

    private final MedicoUseCase medicoUseCase;

    @Override
    public ResponseEntity<MedicoResponse> createMedico(MedicoRequest medicoRequest) {
        Medico medico = medicoUseCase.crear(
                medicoRequest.getNombreCompleto(),
                medicoRequest.getEspecialidad(),
                medicoRequest.getTelefono(),
                medicoRequest.getEmail()
        );
        MedicoResponse response = toResponse(medico);
        return ResponseEntity.created(URI.create("/api/v1/medicos/" + medico.getId()))
                .body(response);
    }

    @Override
    public ResponseEntity<MedicoResponse> getMedico(UUID medicoId) {
        Medico medico = medicoUseCase.obtenerPorId(medicoId);
        return ResponseEntity.ok(toResponse(medico));
    }

    @Override
    public ResponseEntity<List<MedicoResponse>> listMedicos() {
        List<Medico> medicos = medicoUseCase.listarTodos();
        List<MedicoResponse> response = medicos.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MedicoResponse> updateMedico(UUID medicoId, MedicoUpdateRequest medicoUpdateRequest) {
        Medico medico = medicoUseCase.actualizar(
                medicoId,
                medicoUpdateRequest.getNombreCompleto(),
                medicoUpdateRequest.getEspecialidad(),
                medicoUpdateRequest.getTelefono(),
                medicoUpdateRequest.getEmail()
        );
        return ResponseEntity.ok(toResponse(medico));
    }

    private MedicoResponse toResponse(Medico medico) {
        return MedicoResponse.builder()
                .id(medico.getId())
                .nombreCompleto(medico.getNombreCompleto())
                .especialidad(medico.getEspecialidad())
                .telefono(medico.getTelefono())
                .email(medico.getEmail())
                .createdAt(medico.getCreatedAt())
                .updatedAt(medico.getUpdatedAt())
                .build();
    }
}
