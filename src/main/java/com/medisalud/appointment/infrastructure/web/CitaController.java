package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.CitaUseCase;
import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.infrastructure.web.api.CitasApi;
import com.medisalud.appointment.infrastructure.web.dto.CitaRequest;
import com.medisalud.appointment.infrastructure.web.dto.CitaResponse;
import com.medisalud.appointment.infrastructure.web.dto.EstadoCitaEnum;
import com.medisalud.appointment.infrastructure.web.dto.ReprogramarRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CitaController implements CitasApi {

    private final CitaUseCase citaUseCase;

    @Override
    public ResponseEntity<CitaResponse> createCita(CitaRequest citaRequest) {
        Cita cita = citaUseCase.reservar(
                citaRequest.getPacienteId(),
                citaRequest.getMedicoId(),
                citaRequest.getFechaHora()
        );
        CitaResponse response = toResponse(cita);
        return ResponseEntity.created(URI.create("/api/v1/citas/" + cita.getId()))
                .body(response);
    }

    @Override
    public ResponseEntity<CitaResponse> getCita(UUID citaId) {
        Cita cita = citaUseCase.obtenerPorId(citaId);
        return ResponseEntity.ok(toResponse(cita));
    }

    @Override
    public ResponseEntity<List<CitaResponse>> listCitas(UUID medicoId, UUID pacienteId, EstadoCitaEnum estado, LocalDate fechaInicio, LocalDate fechaFin) {
        String estadoStr = estado != null ? estado.name() : null;
        List<Cita> citas = citaUseCase.listarCitas(medicoId, pacienteId, estadoStr, fechaInicio, fechaFin);
        List<CitaResponse> response = citas.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CitaResponse> cancelCita(UUID citaId) {
        Cita cita = citaUseCase.cancelar(citaId, "Cancelado por el paciente");
        return ResponseEntity.ok(toResponse(cita));
    }

    @Override
    public ResponseEntity<CitaResponse> reprogramarCita(UUID citaId, ReprogramarRequest reprogramarRequest) {
        Cita cita = citaUseCase.reprogramar(citaId, reprogramarRequest.getFechaHora());
        return ResponseEntity.ok(toResponse(cita));
    }

    private CitaResponse toResponse(Cita cita) {
        return CitaResponse.builder()
                .id(cita.getId())
                .pacienteId(cita.getPacienteId())
                .medicoId(cita.getMedicoId())
                .fechaHora(cita.getFechaHora())
                .estado(EstadoCitaEnum.valueOf(cita.getEstado()))
                .motivoCancelacion(cita.getMotivoCancelacion())
                .fechaCancelacion(cita.getFechaCancelacion())
                .createdAt(cita.getCreatedAt())
                .updatedAt(cita.getUpdatedAt())
                .build();
    }
}
