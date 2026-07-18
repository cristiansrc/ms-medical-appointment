package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.application.port.output.FestivoRepository;
import com.medisalud.appointment.application.service.FestivoCacheService;
import com.medisalud.appointment.infrastructure.persistence.entity.FestivoEntity;
import com.medisalud.appointment.infrastructure.persistence.repository.FestivoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FestivoRepositoryAdapter implements FestivoRepository {

    private final FestivoJpaRepository jpaRepository;
    private final FestivoCacheService festivoCacheService;

    @Override
    public List<LocalDate> obtenerFestivos(int anio, String pais) {
        festivoCacheService.cargarFestivosSiEsNecesario(anio, pais);
        return jpaRepository.findByYear(anio).stream()
                .filter(f -> pais.equals(f.getCountryCode()))
                .map(FestivoEntity::getDate)
                .toList();
    }

    @Override
    public boolean esFestivo(LocalDate fecha) {
        return festivoCacheService.esFestivo(fecha);
    }
}
