package com.medisalud.appointment.application.service;

import com.medisalud.appointment.infrastructure.client.NagerDateClient;
import com.medisalud.appointment.infrastructure.persistence.entity.FestivoEntity;
import com.medisalud.appointment.infrastructure.persistence.repository.FestivoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivoCacheService {

    private final FestivoJpaRepository festivoJpaRepository;
    private final NagerDateClient nagerDateClient;

    /**
     * Carga los festivos desde Nager.Date API a la BD si no existen para el año dado.
     * Se invoca bajo demanda (lazy/first-access).
     */
    @Transactional
    public List<LocalDate> cargarFestivosSiEsNecesario(int anio, String pais) {
        List<FestivoEntity> existentes = festivoJpaRepository.findByYear(anio);
        if (!existentes.isEmpty()) {
            log.debug("Festivos para {} año {} ya en cache: {} registros", pais, anio, existentes.size());
            return existentes.stream().map(FestivoEntity::getDate).toList();
        }

        log.info("Cargando festivos desde Nager.Date para {} año {}", pais, anio);
        List<NagerDateClient.FestivoDTO> festivosDTO = nagerDateClient.obtenerFestivos(anio, pais);

        if (festivosDTO.isEmpty()) {
            log.warn("No se pudieron obtener festivos de Nager.Date para {} año {}. Sin cache.", pais, anio);
            return List.of();
        }

        List<FestivoEntity> entities = festivosDTO.stream().map(dto -> {
            FestivoEntity entity = new FestivoEntity();
            entity.setId(UUID.randomUUID());
            entity.setDate(dto.getDate());
            entity.setLocalName(dto.getLocalName());
            entity.setName(dto.getName());
            entity.setCountryCode(pais);
            entity.setFixed(dto.getFixed());
            entity.setGlobal(dto.getGlobal());
            entity.setTypes(dto.getTypes() != null ? String.join(",", dto.getTypes()) : null);
            entity.setYear(anio);
            return entity;
        }).toList();

        festivoJpaRepository.saveAll(entities);
        log.info("{} festivos guardados en cache para {} año {}", entities.size(), pais, anio);
        return entities.stream().map(FestivoEntity::getDate).toList();
    }

    /**
     * Verifica si una fecha es festivo, cargando el cache si es necesario.
     */
    @Transactional
    public boolean esFestivo(LocalDate fecha) {
        // Verifica si existe en BD
        boolean existe = festivoJpaRepository.findByDateAndCountryCode(fecha, "CO").isPresent();
        if (existe) return true;

        // Si no existe, intenta cargar del año
        cargarFestivosSiEsNecesario(fecha.getYear(), "CO");
        return festivoJpaRepository.findByDateAndCountryCode(fecha, "CO").isPresent();
    }
}
