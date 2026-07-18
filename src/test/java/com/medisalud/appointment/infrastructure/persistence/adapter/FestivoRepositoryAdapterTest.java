package com.medisalud.appointment.infrastructure.persistence.adapter;

import com.medisalud.appointment.application.port.output.FestivoCachePort;
import com.medisalud.appointment.infrastructure.persistence.entity.FestivoEntity;
import com.medisalud.appointment.infrastructure.persistence.repository.FestivoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FestivoRepositoryAdapterTest {

    @Mock
    private FestivoJpaRepository jpaRepository;

    @Mock
    private FestivoCachePort festivoCachePort;

    @InjectMocks
    private FestivoRepositoryAdapter adapter;

    @Test
    @DisplayName("obtenerFestivos: retorna lista de festivos del año y pais")
    void should_ObtenerFestivos() {
        int anio = 2026;
        String pais = "CO";

        FestivoEntity entity = new FestivoEntity();
        entity.setDate(LocalDate.of(2026, 1, 1));
        entity.setCountryCode("CO");
        entity.setYear(2026);

        FestivoEntity entity2 = new FestivoEntity();
        entity2.setDate(LocalDate.of(2026, 12, 25));
        entity2.setCountryCode("CO");
        entity2.setYear(2026);

        when(jpaRepository.findByYear(anio)).thenReturn(List.of(entity, entity2));

        List<LocalDate> result = adapter.obtenerFestivos(anio, pais);

        assertEquals(2, result.size());
        assertTrue(result.contains(LocalDate.of(2026, 1, 1)));
        assertTrue(result.contains(LocalDate.of(2026, 12, 25)));
        verify(festivoCachePort).cargarFestivosSiEsNecesario(anio, pais);
    }

    @Test
    @DisplayName("obtenerFestivos: filtra por pais correctamente")
    void should_FilterByCountry() {
        int anio = 2026;

        FestivoEntity entityCO = new FestivoEntity();
        entityCO.setDate(LocalDate.of(2026, 1, 1));
        entityCO.setCountryCode("CO");
        entityCO.setYear(2026);

        FestivoEntity entityMX = new FestivoEntity();
        entityMX.setDate(LocalDate.of(2026, 2, 5));
        entityMX.setCountryCode("MX");
        entityMX.setYear(2026);

        when(jpaRepository.findByYear(anio)).thenReturn(List.of(entityCO, entityMX));

        List<LocalDate> result = adapter.obtenerFestivos(anio, "CO");

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2026, 1, 1), result.get(0));
    }

    @Test
    @DisplayName("esFestivo: delega al cache port")
    void should_DelegateEsFestivo() {
        LocalDate fecha = LocalDate.of(2026, 1, 1);
        when(festivoCachePort.esFestivo(fecha)).thenReturn(true);

        assertTrue(adapter.esFestivo(fecha));

        when(festivoCachePort.esFestivo(fecha)).thenReturn(false);
        assertFalse(adapter.esFestivo(fecha));
    }
}
