package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.PacienteUseCase;
import com.medisalud.appointment.domain.model.Paciente;
import com.medisalud.appointment.infrastructure.web.api.PacientesApi;
import com.medisalud.appointment.infrastructure.web.dto.PacienteRequest;
import com.medisalud.appointment.infrastructure.web.dto.PacienteResponse;
import com.medisalud.appointment.infrastructure.web.dto.PacienteUpdateRequest;
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
public class PacienteController implements PacientesApi {

    private final PacienteUseCase pacienteUseCase;

    @Override
    public ResponseEntity<PacienteResponse> createPaciente(PacienteRequest pacienteRequest) {
        Paciente paciente = pacienteUseCase.crear(
                pacienteRequest.getNombreCompleto(),
                pacienteRequest.getDocumentoIdentidad(),
                pacienteRequest.getTelefono(),
                pacienteRequest.getEmail(),
                pacienteRequest.getBirthDate()
        );
        PacienteResponse response = toResponse(paciente);
        return ResponseEntity.created(URI.create("/api/v1/pacientes/" + paciente.getId()))
                .body(response);
    }

    @Override
    public ResponseEntity<PacienteResponse> getPaciente(UUID pacienteId) {
        Paciente paciente = pacienteUseCase.obtenerPorId(pacienteId);
        return ResponseEntity.ok(toResponse(paciente));
    }

    @Override
    public ResponseEntity<List<PacienteResponse>> listPacientes() {
        List<Paciente> pacientes = pacienteUseCase.listarTodos();
        List<PacienteResponse> response = pacientes.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PacienteResponse> updatePaciente(UUID pacienteId, PacienteUpdateRequest pacienteUpdateRequest) {
        Paciente paciente = pacienteUseCase.actualizar(
                pacienteId,
                pacienteUpdateRequest.getNombreCompleto(),
                pacienteUpdateRequest.getTelefono(),
                pacienteUpdateRequest.getEmail(),
                pacienteUpdateRequest.getBirthDate()
        );
        return ResponseEntity.ok(toResponse(paciente));
    }

    private PacienteResponse toResponse(Paciente paciente) {
        return PacienteResponse.builder()
                .id(paciente.getId())
                .nombreCompleto(paciente.getNombreCompleto())
                .documentoIdentidad(paciente.getDocumentoIdentidad())
                .telefono(paciente.getTelefono())
                .email(paciente.getEmail())
                .birthDate(paciente.getFechaNacimiento())
                .createdAt(paciente.getCreatedAt())
                .updatedAt(paciente.getUpdatedAt())
                .build();
    }
}
