package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.application.port.input.CitaUseCase;
import com.medisalud.appointment.domain.model.FranjaHoraria;
import com.medisalud.appointment.infrastructure.web.api.DisponibilidadApi;
import com.medisalud.appointment.infrastructure.web.dto.DisponibilidadResponse;
import com.medisalud.appointment.infrastructure.web.dto.FranjaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DisponibilidadController implements DisponibilidadApi {

    private final CitaUseCase citaUseCase;

    @Override
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidad(UUID medicoId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Generate all available franjas for each day in the range, filtering only available ones
        long daysBetween = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        List<FranjaHoraria> todasFranjas = Stream.iterate(fechaInicio, date -> date.plusDays(1))
                .limit(daysBetween + 1)
                .flatMap(fecha -> citaUseCase.consultarDisponibilidad(medicoId, fecha).stream())
                .filter(FranjaHoraria::isDisponible)
                .toList();

        List<FranjaResponse> franjasResponse = todasFranjas.stream()
                .map(f -> FranjaResponse.builder()
                        .inicio(f.getInicio())
                        .fin(f.getFin())
                        .build())
                .toList();

        DisponibilidadResponse response = DisponibilidadResponse.builder()
                .medicoId(medicoId)
                .franjas(franjasResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
