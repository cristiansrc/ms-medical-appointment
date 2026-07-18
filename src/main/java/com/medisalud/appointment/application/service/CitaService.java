package com.medisalud.appointment.application.service;

import com.medisalud.appointment.application.port.input.CitaUseCase;
import com.medisalud.appointment.application.port.output.CitaRepository;
import com.medisalud.appointment.application.port.output.FestivoRepository;
import com.medisalud.appointment.application.port.output.MedicoRepository;
import com.medisalud.appointment.application.port.output.PacienteRepository;
import com.medisalud.appointment.application.port.output.PenalizacionRepository;
import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ConflictException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.domain.model.Cita;
import com.medisalud.appointment.domain.model.FranjaHoraria;
import com.medisalud.appointment.domain.model.Medico;
import com.medisalud.appointment.domain.model.Paciente;
import com.medisalud.appointment.domain.model.RegistroPenalizacion;
import com.medisalud.appointment.domain.service.ValidadorReglasNegocio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaService implements CitaUseCase {

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final PenalizacionRepository penalizacionRepository;
    private final FestivoRepository festivoRepository;

    private static final int FRANJA_MINUTOS = 30;

    @Override
    @Transactional
    public Cita reservar(UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora) {
        // Validar existencia
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Medico", medicoId));
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", pacienteId));

        // RN-01: Validar franja horaria
        if (!ValidadorReglasNegocio.esFranjaHorariaValida(fechaHora)) {
            throw new BusinessException("INVALID_SLOT", "Las citas solo estan disponibles de Lunes a Viernes 8:00-18:00 y Sabados 8:00-13:00");
        }
        if (!ValidadorReglasNegocio.esFranjaDe30Minutos(fechaHora)) {
            throw new BusinessException("INVALID_SLOT", "Las citas solo se agendan en franjas de 30 minutos (minutos 0 o 30)");
        }

        // RN-01b: No domingos ni festivos
        LocalDate fechaLocal = fechaHora.toLocalDate();
        if (festivoRepository.esFestivo(fechaLocal)) {
            throw new BusinessException("INVALID_SLOT", "No se pueden agendar citas en dias festivos");
        }

        // RN-03: Fecha de nacimiento no futura
        if (paciente.getFechaNacimiento() != null && !ValidadorReglasNegocio.esFechaNacimientoValida(paciente.getFechaNacimiento())) {
            throw new BusinessException("INVALID_BIRTH_DATE", "La fecha de nacimiento no puede ser futura");
        }

        // RN-05: Verificar bloqueo por penalizaciones
        OffsetDateTime treintaDiasAtras = fechaHora.minusDays(30);
        int penalizaciones = penalizacionRepository.countByPacienteIdAndFechaAfter(pacienteId, treintaDiasAtras);
        if (ValidadorReglasNegocio.excedeLimitePenalizaciones(penalizaciones)) {
            throw new BusinessException("PACIENTE_BLOCKED",
                    "El paciente tiene " + penalizaciones + " cancelaciones tardias en los ultimos 30 dias. No puede agendar nuevas citas.");
        }

        // RN-02: Validar conflicto medico
        OffsetDateTime finFranja = fechaHora.plusMinutes(FRANJA_MINUTOS);
        List<Cita> citasMedico = citaRepository.findByMedicoIdAndFechaBetween(medicoId, fechaHora, finFranja);
        boolean medicoOcupado = citasMedico.stream()
                .anyMatch(c -> "PROGRAMADA".equals(c.getEstado()));
        if (medicoOcupado) {
            throw new ConflictException("MEDICO_SLOT_CONFLICT",
                    "El medico no esta disponible en la franja solicitada");
        }

        // RN-04: Validar conflicto paciente
        List<Cita> citasPaciente = citaRepository.findByPacienteIdAndFechaBetween(pacienteId, fechaHora, finFranja);
        boolean pacienteOcupado = citasPaciente.stream()
                .anyMatch(c -> "PROGRAMADA".equals(c.getEstado()));
        if (pacienteOcupado) {
            throw new ConflictException("PACIENTE_SLOT_CONFLICT",
                    "El paciente ya tiene una cita programada en esta franja horaria");
        }

        // Crear cita
        Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, fechaHora);
        Cita saved = citaRepository.save(cita);
        log.info("Cita creada: {} - Paciente: {} - Medico: {} - Fecha: {}",
                saved.getId(), pacienteId, medicoId, fechaHora);
        return saved;
    }

    @Override
    @Transactional
    public Cita cancelar(UUID citaId, String motivo) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", citaId));

        if (!"PROGRAMADA".equals(cita.getEstado())) {
            throw new BusinessException("CITA_ALREADY_CANCELLED", "La cita ya fue cancelada anteriormente");
        }

        // RN-05: Verificar si la cancelación es tardía (≤ 2h antes)
        boolean esTardia = ValidadorReglasNegocio.esCancelacionTardia(cita.getFechaHora());

        cita.cancelar(motivo);
        Cita saved = citaRepository.save(cita);

        if (esTardia) {
            RegistroPenalizacion penalizacion = new RegistroPenalizacion(
                    UUID.randomUUID(), cita.getPacienteId(), citaId);
            penalizacionRepository.save(penalizacion);
            log.warn("Penalizacion registrada para paciente {} por cancelacion tardia de cita {}",
                    cita.getPacienteId(), citaId);
        }

        log.info("Cita cancelada: {} - Motivo: {}", citaId, motivo);
        return saved;
    }

    @Override
    @Transactional
    public Cita reprogramar(UUID citaId, OffsetDateTime nuevaFechaHora) {
        Cita citaOriginal = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", citaId));

        if (!"PROGRAMADA".equals(citaOriginal.getEstado())) {
            throw new BusinessException("CITA_ALREADY_CANCELLED", "No se puede reprogramar una cita cancelada");
        }

        // RN-01: Validar nueva franja
        if (!ValidadorReglasNegocio.esFranjaHorariaValida(nuevaFechaHora)) {
            throw new BusinessException("INVALID_SLOT", "La nueva fecha/hora no esta dentro del horario permitido");
        }
        if (!ValidadorReglasNegocio.esFranjaDe30Minutos(nuevaFechaHora)) {
            throw new BusinessException("INVALID_SLOT", "Las citas solo se agendan en franjas de 30 minutos");
        }

        // RN-01b: Validar que la nueva fecha no sea festivo
        LocalDate nuevaFechaLocal = nuevaFechaHora.toLocalDate();
        if (festivoRepository.esFestivo(nuevaFechaLocal)) {
            throw new BusinessException("INVALID_SLOT", "No se pueden agendar citas en dias festivos");
        }

        // RN-02: Validar medico disponible en la nueva franja (excluyendo la cita original)
        OffsetDateTime finFranja = nuevaFechaHora.plusMinutes(FRANJA_MINUTOS);
        List<Cita> citasMedico = citaRepository.findByMedicoIdAndFechaBetween(
                citaOriginal.getMedicoId(), nuevaFechaHora, finFranja);
        boolean medicoOcupado = citasMedico.stream()
                .anyMatch(c -> "PROGRAMADA".equals(c.getEstado()) && !c.getId().equals(citaId));
        if (medicoOcupado) {
            throw new ConflictException("MEDICO_SLOT_CONFLICT",
                    "El medico no esta disponible en la nueva franja horaria");
        }

        // RN-04: Validar paciente disponible en la nueva franja (excluyendo la cita original)
        List<Cita> citasPaciente = citaRepository.findByPacienteIdAndFechaBetween(
                citaOriginal.getPacienteId(), nuevaFechaHora, finFranja);
        boolean pacienteOcupado = citasPaciente.stream()
                .anyMatch(c -> "PROGRAMADA".equals(c.getEstado()) && !c.getId().equals(citaId));
        if (pacienteOcupado) {
            throw new ConflictException("PACIENTE_SLOT_CONFLICT",
                    "El paciente ya tiene una cita en la nueva franja horaria");
        }

        // RN-05: Verificar bloqueo por penalizaciones (desde nueva fecha hacia atras 30 dias)
        OffsetDateTime treintaDiasAtras = nuevaFechaHora.minusDays(30);
        int penalizaciones = penalizacionRepository.countByPacienteIdAndFechaAfter(
                citaOriginal.getPacienteId(), treintaDiasAtras);
        if (ValidadorReglasNegocio.excedeLimitePenalizaciones(penalizaciones)) {
            throw new BusinessException("PACIENTE_BLOCKED",
                    "El paciente tiene " + penalizaciones + " cancelaciones tardias en los ultimos 30 dias. No puede reprogramar.");
        }

        // RN-05: Aplicar penalización por cancelación de la cita original si es tardía
        boolean esTardia = ValidadorReglasNegocio.esCancelacionTardia(citaOriginal.getFechaHora());

        // Cancelar cita original
        citaOriginal.cancelar("Reprogramada");
        citaRepository.save(citaOriginal);

        if (esTardia) {
            RegistroPenalizacion penalizacion = new RegistroPenalizacion(
                    UUID.randomUUID(), citaOriginal.getPacienteId(), citaId);
            penalizacionRepository.save(penalizacion);
            log.warn("Penalizacion registrada para paciente {} por reprogramacion tardia de cita {}",
                    citaOriginal.getPacienteId(), citaId);
        }

        // Crear nueva cita
        Cita nuevaCita = new Cita(UUID.randomUUID(), citaOriginal.getPacienteId(),
                citaOriginal.getMedicoId(), nuevaFechaHora);
        Cita saved = citaRepository.save(nuevaCita);

        log.info("Cita reprogramada: {} (original {}) - Nueva fecha: {}", saved.getId(), citaId, nuevaFechaHora);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FranjaHoraria> consultarDisponibilidad(UUID medicoId, LocalDate fecha) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Medico", medicoId));

        // Verificar si es domingo o festivo
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of(); // No hay disponibilidad en domingos
        }
        if (festivoRepository.esFestivo(fecha)) {
            return List.of(); // No hay disponibilidad en festivos
        }

        // Generar todas las franjas del día según horario
        List<FranjaHoraria> franjas = new ArrayList<>();
        OffsetDateTime inicioDia = fecha.atStartOfDay(ZoneOffset.ofHours(-5)).toOffsetDateTime()
                .withHour(8).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime finDia;
        if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY) {
            finDia = inicioDia.withHour(13).withMinute(0);
        } else {
            finDia = inicioDia.withHour(18).withMinute(0);
        }

        // Obtener citas del medico en ese día
        List<Cita> citasDelDia = citaRepository.findByMedicoIdAndFechaBetween(medicoId, inicioDia, finDia);

        OffsetDateTime franjaActual = inicioDia;
        while (franjaActual.isBefore(finDia)) {
            OffsetDateTime finFranja = franjaActual.plusMinutes(FRANJA_MINUTOS);
            OffsetDateTime inicioFranja = franjaActual;
            boolean ocupado = citasDelDia.stream()
                    .anyMatch(c -> "PROGRAMADA".equals(c.getEstado())
                            && !c.getFechaHora().isBefore(inicioFranja)
                            && c.getFechaHora().isBefore(finFranja));
            franjas.add(new FranjaHoraria(medicoId, inicioFranja, finFranja, !ocupado));
            franjaActual = finFranja;
        }

        return franjas;
    }

    @Override
    @Transactional(readOnly = true)
    public Cita obtenerPorId(UUID id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas(UUID medicoId, UUID pacienteId, String estado, LocalDate fechaInicio, LocalDate fechaFin) {
        return citaRepository.findAllWithFilters(medicoId, pacienteId, estado, fechaInicio, fechaFin);
    }
}
