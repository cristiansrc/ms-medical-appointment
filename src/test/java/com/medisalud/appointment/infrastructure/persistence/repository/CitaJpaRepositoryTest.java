package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.CitaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CitaJpaRepositoryTest {

    @Autowired
    private CitaJpaRepository citaJpaRepository;

    @Test
    @DisplayName("findByMedicoIdAndFechaHoraBetween retorna citas del medico")
    void findByMedicoAndFecha() {
        UUID medicoId = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();

        CitaEntity cita = new CitaEntity();
        cita.setId(UUID.randomUUID());
        cita.setMedicoId(medicoId);
        cita.setPacienteId(UUID.randomUUID());
        cita.setFechaHora(ahora);
        cita.setEstado("PROGRAMADA");
        citaJpaRepository.save(cita);

        var found = citaJpaRepository.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                medicoId, ahora.minusHours(1), ahora.plusHours(1));
        assertEquals(1, found.size());
    }

    @Test
    @DisplayName("findAllWithFilters con filtros basicos")
    void findAllWithFilters() {
        UUID medicoId = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();

        CitaEntity cita = new CitaEntity();
        cita.setId(UUID.randomUUID());
        cita.setMedicoId(medicoId);
        cita.setPacienteId(UUID.randomUUID());
        cita.setFechaHora(ahora);
        cita.setEstado("PROGRAMADA");
        citaJpaRepository.save(cita);

        var result = citaJpaRepository.findAllWithFilters(medicoId, null, "PROGRAMADA", ahora.toLocalDate());
        assertEquals(1, result.size());
    }
}
