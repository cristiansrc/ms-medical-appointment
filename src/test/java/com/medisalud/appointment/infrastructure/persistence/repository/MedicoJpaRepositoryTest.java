package com.medisalud.appointment.infrastructure.persistence.repository;

import com.medisalud.appointment.infrastructure.persistence.entity.MedicoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MedicoJpaRepositoryTest {

    @Autowired
    private MedicoJpaRepository medicoJpaRepository;

    @Test
    @DisplayName("Guardar y encontrar medico por ID")
    void saveAndFindById() {
        MedicoEntity entity = new MedicoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNombreCompleto("Dr. Test");
        entity.setEspecialidad("Cardiologia");

        MedicoEntity saved = medicoJpaRepository.save(entity);
        assertNotNull(saved.getId());

        var found = medicoJpaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Dr. Test", found.get().getNombreCompleto());
    }

    @Test
    @DisplayName("Listar todos los medicos")
    void findAll() {
        MedicoEntity e1 = new MedicoEntity();
        e1.setId(UUID.randomUUID());
        e1.setNombreCompleto("Dr. Uno");
        e1.setEspecialidad("Cardiologia");

        MedicoEntity e2 = new MedicoEntity();
        e2.setId(UUID.randomUUID());
        e2.setNombreCompleto("Dr. Dos");
        e2.setEspecialidad("Pediatria");

        medicoJpaRepository.save(e1);
        medicoJpaRepository.save(e2);

        var all = medicoJpaRepository.findAll();
        assertEquals(2, all.size());
    }
}
