package com.medisalud.appointment.application.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PenalizacionRepository penalizacionRepository;

    @Mock
    private FestivoRepository festivoRepository;

    @InjectMocks
    private CitaService citaService;

    private UUID medicoId;
    private UUID pacienteId;
    private Medico medico;
    private Paciente paciente;
    private OffsetDateTime fechaValida;

    @BeforeEach
    void setUp() {
        medicoId = UUID.randomUUID();
        pacienteId = UUID.randomUUID();
        medico = new Medico(medicoId, "Dr. Test", "Cardiologia", "555-0000", "test@test.com");
        paciente = new Paciente(pacienteId, "Paciente Test", "12345678", "555-0001", "p@test.com",
                LocalDate.now().minusYears(30));
        // Lunes 20 de Julio 2026 10:00 -05:00 (dia habil, no festivo)
        fechaValida = LocalDate.of(2026, 7, 20).atTime(10, 0).atOffset(ZoneOffset.ofHours(-5));
    }

    // ========================
    // RESERVAR (crear cita)
    // ========================

    @Nested
    @DisplayName("Reservar cita")
    class ReservarTest {

        @Test
        @DisplayName("Happy path: crear cita cuando todas las validaciones pasan")
        void should_CreateCita_when_AllValidationsPass() {
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(festivoRepository.esFestivo(fechaValida.toLocalDate())).thenReturn(false);
            when(penalizacionRepository.countByPacienteIdAndFechaAfter(any(), any())).thenReturn(0);
            when(citaRepository.findByMedicoIdAndFechaBetween(any(), any(), any())).thenReturn(List.of());
            when(citaRepository.findByPacienteIdAndFechaBetween(any(), any(), any())).thenReturn(List.of());
            when(citaRepository.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

            Cita result = citaService.reservar(pacienteId, medicoId, fechaValida);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals("PROGRAMADA", result.getEstado());
            assertEquals(pacienteId, result.getPacienteId());
            assertEquals(medicoId, result.getMedicoId());
            verify(citaRepository).save(any(Cita.class));
        }

        @Test
        @DisplayName("Lanza BusinessException cuando la franja horaria es invalida (domingo)")
        void should_ThrowBusinessException_when_InvalidSchedule_Sunday() {
            OffsetDateTime domingo = LocalDate.of(2026, 7, 26).atTime(10, 0).atOffset(ZoneOffset.ofHours(-5));
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reservar(pacienteId, medicoId, domingo));
            assertEquals("INVALID_SCHEDULE", ex.getCode());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando la franja horaria es invalida (fuera de horario)")
        void should_ThrowBusinessException_when_InvalidSchedule_OutOfHours() {
            OffsetDateTime fueraHorario = LocalDate.of(2026, 7, 20).atTime(18, 0).atOffset(ZoneOffset.ofHours(-5));
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fueraHorario));
            assertEquals("INVALID_SCHEDULE", ex.getCode());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando los minutos no son :00 ni :30")
        void should_ThrowBusinessException_when_InvalidSlot() {
            OffsetDateTime minutosInvalidos = LocalDate.of(2026, 7, 20).atTime(10, 15).atOffset(ZoneOffset.ofHours(-5));
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reservar(pacienteId, medicoId, minutosInvalidos));
            assertEquals("INVALID_SLOT", ex.getCode());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando es dia festivo")
        void should_ThrowBusinessException_when_Holiday() {
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(festivoRepository.esFestivo(fechaValida.toLocalDate())).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
            assertEquals("HOLIDAY", ex.getCode());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando el paciente es menor de edad")
        void should_ThrowBusinessException_when_UnderagePatient() {
            Paciente menor = new Paciente(pacienteId, "Menor", "87654321", "555-0002", "m@test.com",
                    LocalDate.now().minusYears(17));
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(menor));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
            assertEquals("UNDERAGE", ex.getCode());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando el paciente esta bloqueado (3+ penalizaciones)")
        void should_ThrowBusinessException_when_PatientBlocked() {
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(festivoRepository.esFestivo(fechaValida.toLocalDate())).thenReturn(false);
            when(penalizacionRepository.countByPacienteIdAndFechaAfter(any(), any())).thenReturn(3);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
            assertEquals("BLOCKED_PATIENT", ex.getCode());
        }

        @Test
        @DisplayName("Lanza ConflictException cuando el medico ya tiene cita en esa franja")
        void should_ThrowConflictException_when_MedicoSlotOccupied() {
            Cita citaExistente = new Cita(UUID.randomUUID(), pacienteId, medicoId, fechaValida);
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(festivoRepository.esFestivo(fechaValida.toLocalDate())).thenReturn(false);
            when(penalizacionRepository.countByPacienteIdAndFechaAfter(any(), any())).thenReturn(0);
            when(citaRepository.findByMedicoIdAndFechaBetween(any(), any(), any()))
                    .thenReturn(List.of(citaExistente));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
            assertEquals("MEDICO_SLOT_CONFLICT", ex.getCode());
        }

        @Test
        @DisplayName("Lanza ConflictException cuando el paciente ya tiene cita en esa franja")
        void should_ThrowConflictException_when_PatientSlotOccupied() {
            Cita citaExistente = new Cita(UUID.randomUUID(), pacienteId, medicoId, fechaValida);
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(festivoRepository.esFestivo(fechaValida.toLocalDate())).thenReturn(false);
            when(penalizacionRepository.countByPacienteIdAndFechaAfter(any(), any())).thenReturn(0);
            when(citaRepository.findByMedicoIdAndFechaBetween(any(), any(), any())).thenReturn(List.of());
            when(citaRepository.findByPacienteIdAndFechaBetween(any(), any(), any()))
                    .thenReturn(List.of(citaExistente));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
            assertEquals("PATIENT_SLOT_CONFLICT", ex.getCode());
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el medico no existe")
        void should_ThrowResourceNotFoundException_when_MedicoNotFound() {
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el paciente no existe")
        void should_ThrowResourceNotFoundException_when_PacienteNotFound() {
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> citaService.reservar(pacienteId, medicoId, fechaValida));
        }
    }

    // ========================
    // CANCELAR
    // ========================

    @Nested
    @DisplayName("Cancelar cita")
    class CancelarTest {

        @Test
        @DisplayName("Cancela cita exitosamente sin penalizacion (con suficiente antelacion)")
        void should_CancelCita_when_Valid() {
            OffsetDateTime citaFutura = OffsetDateTime.now().plusDays(7);
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, citaFutura);
            when(citaRepository.findById(cita.getId())).thenReturn(Optional.of(cita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

            Cita result = citaService.cancelar(cita.getId(), "Motivo valido");

            assertEquals("CANCELADA", result.getEstado());
            assertEquals("Motivo valido", result.getMotivoCancelacion());
            assertNotNull(result.getFechaCancelacion());
            verify(penalizacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cancela cita y registra penalizacion cuando es tardia (<= 2h)")
        void should_CancelCita_and_RegisterPenalty_when_LateCancellation() {
            OffsetDateTime citaProxima = OffsetDateTime.now().plusHours(1);
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, citaProxima);
            when(citaRepository.findById(cita.getId())).thenReturn(Optional.of(cita));
            when(citaRepository.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

            Cita result = citaService.cancelar(cita.getId(), "Cancelacion tardia");

            assertEquals("CANCELADA", result.getEstado());
            verify(penalizacionRepository).save(any());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando la cita ya estaba cancelada")
        void should_ThrowBusinessException_when_AlreadyCancelled() {
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, OffsetDateTime.now().plusDays(1));
            cita.cancelar("Ya cancelada");
            when(citaRepository.findById(cita.getId())).thenReturn(Optional.of(cita));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.cancelar(cita.getId(), "Otra cancelacion"));
            assertEquals("ALREADY_CANCELLED", ex.getCode());
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la cita no existe")
        void should_ThrowResourceNotFoundException_when_CitaNotFound() {
            UUID citaId = UUID.randomUUID();
            when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> citaService.cancelar(citaId, "Motivo"));
        }
    }

    // ========================
    // REPROGRAMAR
    // ========================

    @Nested
    @DisplayName("Reprogramar cita")
    class ReprogramarTest {

        @Test
        @DisplayName("Reprograma cita exitosamente a nueva fecha valida")
        void should_ReprogramCita_when_Valid() {
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, OffsetDateTime.now().plusDays(1));
            OffsetDateTime nuevaFecha = LocalDate.now().plusDays(2)
                    .atTime(10, 0).atOffset(ZoneOffset.ofHours(-5));
            when(citaRepository.findById(cita.getId())).thenReturn(Optional.of(cita));
            when(citaRepository.findByMedicoIdAndFechaBetween(any(), any(), any())).thenReturn(List.of());
            when(citaRepository.findByPacienteIdAndFechaBetween(any(), any(), any())).thenReturn(List.of());
            when(citaRepository.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

            Cita result = citaService.reprogramar(cita.getId(), nuevaFecha);

            assertEquals(nuevaFecha, result.getFechaHora());
            verify(citaRepository).save(any(Cita.class));
        }

        @Test
        @DisplayName("Lanza BusinessException cuando se reprograma una cita cancelada")
        void should_Throw_when_ReprogramCancelledCita() {
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, OffsetDateTime.now().plusDays(1));
            cita.cancelar("Ya cancelada");
            when(citaRepository.findById(cita.getId())).thenReturn(Optional.of(cita));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reprogramar(cita.getId(), OffsetDateTime.now().plusDays(2)));
            assertEquals("ALREADY_CANCELLED", ex.getCode());
        }

        @Test
        @DisplayName("Lanza BusinessException cuando la nueva franja es invalida")
        void should_ThrowBusinessException_when_InvalidSchedule() {
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, OffsetDateTime.now().plusDays(1));
            OffsetDateTime domingo = LocalDate.of(2026, 7, 26).atTime(10, 0).atOffset(ZoneOffset.ofHours(-5));
            when(citaRepository.findById(cita.getId())).thenReturn(Optional.of(cita));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> citaService.reprogramar(cita.getId(), domingo));
            assertEquals("INVALID_SCHEDULE", ex.getCode());
        }
    }

    // ========================
    // LISTAR CITAS
    // ========================

    @Nested
    @DisplayName("Listar citas")
    class ListarCitasTest {

        @Test
        @DisplayName("Retorna lista vacia cuando no hay filtros y no hay citas")
        void should_ListCitas_when_NoFilters() {
            when(citaRepository.findAllWithFilters(null, null, null, null)).thenReturn(List.of());

            List<Cita> result = citaService.listarCitas(null, null, null, null);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Retorna citas filtradas cuando se aplican filtros")
        void should_ListCitas_when_WithFilters() {
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, OffsetDateTime.now());
            when(citaRepository.findAllWithFilters(eq(medicoId), eq(pacienteId), eq("PROGRAMADA"), any()))
                    .thenReturn(List.of(cita));

            List<Cita> result = citaService.listarCitas(medicoId, pacienteId, "PROGRAMADA", LocalDate.now());

            assertEquals(1, result.size());
            assertEquals(medicoId, result.get(0).getMedicoId());
        }
    }

    // ========================
    // CONSULTAR DISPONIBILIDAD
    // ========================

    @Nested
    @DisplayName("Consultar disponibilidad")
    class ConsultarDisponibilidadTest {

        @Test
        @DisplayName("Retorna franjas disponibles para un dia laboral sin citas")
        void should_ConsultDisponibilidad_when_ValidRequest() {
            LocalDate lunes = LocalDate.of(2026, 7, 20);
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(festivoRepository.esFestivo(lunes)).thenReturn(false);
            when(citaRepository.findByMedicoIdAndFechaBetween(any(), any(), any())).thenReturn(List.of());

            List<FranjaHoraria> result = citaService.consultarDisponibilidad(medicoId, lunes);

            assertFalse(result.isEmpty());
            // 7:00 a 17:00 = 10h = 20 franjas de 30 min
            assertEquals(20, result.size());
            assertTrue(result.stream().allMatch(FranjaHoraria::isDisponible));
        }

        @Test
        @DisplayName("Retorna lista vacia cuando es domingo")
        void should_ConsultDisponibilidad_when_Sunday_returnEmpty() {
            LocalDate domingo = LocalDate.of(2026, 7, 26);
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));

            List<FranjaHoraria> result = citaService.consultarDisponibilidad(medicoId, domingo);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Retorna lista vacia cuando es festivo")
        void should_ConsultDisponibilidad_when_Holiday_returnEmpty() {
            LocalDate lunes = LocalDate.of(2026, 7, 20);
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(festivoRepository.esFestivo(lunes)).thenReturn(true);

            List<FranjaHoraria> result = citaService.consultarDisponibilidad(medicoId, lunes);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Retorna franjas ocupadas cuando el medico tiene citas")
        void should_ReturnOccupiedSlots_when_MedicoHasCitas() {
            LocalDate lunes = LocalDate.of(2026, 7, 20);
            OffsetDateTime franjaOcupada = lunes.atTime(10, 0).atOffset(ZoneOffset.ofHours(-5));
            Cita cita = new Cita(UUID.randomUUID(), pacienteId, medicoId, franjaOcupada);

            when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
            when(festivoRepository.esFestivo(lunes)).thenReturn(false);
            when(citaRepository.findByMedicoIdAndFechaBetween(any(), any(), any())).thenReturn(List.of(cita));

            List<FranjaHoraria> result = citaService.consultarDisponibilidad(medicoId, lunes);

            assertEquals(20, result.size());
            // Franja de 10:00 debe estar ocupada
            FranjaHoraria franjaOcupadaResult = result.stream()
                    .filter(f -> f.getInicio().equals(franjaOcupada))
                    .findFirst().orElseThrow();
            assertFalse(franjaOcupadaResult.isDisponible());
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el medico no existe")
        void should_ThrowResourceNotFoundException_when_MedicoNotFound() {
            when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> citaService.consultarDisponibilidad(medicoId, LocalDate.now()));
        }
    }

    // ========================
    // OBTENER POR ID
    // ========================

    @Nested
    @DisplayName("Obtener cita por ID")
    class ObtenerPorIdTest {

        @Test
        @DisplayName("Retorna cita cuando existe")
        void should_ReturnCita_when_Exists() {
            UUID citaId = UUID.randomUUID();
            Cita cita = new Cita(citaId, pacienteId, medicoId, OffsetDateTime.now());
            when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));

            Cita result = citaService.obtenerPorId(citaId);

            assertEquals(citaId, result.getId());
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando no existe")
        void should_Throw_when_NotFound() {
            UUID citaId = UUID.randomUUID();
            when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> citaService.obtenerPorId(citaId));
        }
    }
}
