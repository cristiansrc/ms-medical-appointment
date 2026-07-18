package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.PacienteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PacienteJpaRepositoryTest {

    @Autowired
    private PacienteJpaRepository pacienteJpaRepository;

    @Test
    @DisplayName("existsByDocumentoIdentidad retorna true si existe")
    void existsByDocumento() {
        PacienteEntity entity = new PacienteEntity();
        entity.setId(UUID.randomUUID());
        entity.setNombreCompleto("Paciente Test");
        entity.setDocumentoIdentidad("12345678");
        entity.setTelefono("555-0000");
        entity.setEmail("test@test.com");
        pacienteJpaRepository.save(entity);

        assertTrue(pacienteJpaRepository.existsByDocumentoIdentidad("12345678"));
        assertFalse(pacienteJpaRepository.existsByDocumentoIdentidad("00000000"));
    }
}
