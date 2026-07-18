package com.medisalud.appointment.infrastructure.service;

import com.medisalud.appointment.infrastructure.client.NagerDateClient;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FestivoCacheServiceTest {

    @Mock
    private FestivoJpaRepository festivoJpaRepository;

    @Mock
    private NagerDateClient nagerDateClient;

    @InjectMocks
    private FestivoCacheService festivoCacheService;

    @Test
    @DisplayName("cargarFestivosSiEsNecesario: retorna festivos existentes sin llamar a API")
    void should_CargarFestivos_when_CacheVacio() {
        int anio = 2026;
        String pais = "CO";
        FestivoEntity entity = new FestivoEntity();
        entity.setDate(LocalDate.of(2026, 1, 1));
        entity.setCountryCode("CO");
        entity.setYear(2026);

        when(festivoJpaRepository.findByYear(anio)).thenReturn(List.of(entity));

        List<LocalDate> result = festivoCacheService.cargarFestivosSiEsNecesario(anio, pais);

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2026, 1, 1), result.get(0));
        verify(nagerDateClient, never()).obtenerFestivos(anyInt(), anyString());
    }

    @Test
    @DisplayName("cargarFestivosSiEsNecesario: carga desde API cuando cache vacio")
    void should_ReturnCached_when_AlreadyLoaded() {
        int anio = 2026;
        String pais = "CO";
        NagerDateClient.FestivoDTO dto = new NagerDateClient.FestivoDTO();
        dto.setDate(LocalDate.of(2026, 1, 1));
        dto.setLocalName("Año Nuevo");
        dto.setName("New Year");
        dto.setFixed(true);
        dto.setGlobal(true);
        dto.setTypes(new String[]{"Public"});

        when(festivoJpaRepository.findByYear(anio)).thenReturn(List.of());
        when(nagerDateClient.obtenerFestivos(anio, pais)).thenReturn(List.of(dto));

        List<LocalDate> result = festivoCacheService.cargarFestivosSiEsNecesario(anio, pais);

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2026, 1, 1), result.get(0));
        verify(festivoJpaRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("cargarFestivosSiEsNecesario: retorna lista vacia cuando API falla")
    void should_ReturnEmpty_when_ApiFails() {
        int anio = 2026;
        String pais = "CO";

        when(festivoJpaRepository.findByYear(anio)).thenReturn(List.of());
        when(nagerDateClient.obtenerFestivos(anio, pais)).thenReturn(List.of());

        List<LocalDate> result = festivoCacheService.cargarFestivosSiEsNecesario(anio, pais);

        assertTrue(result.isEmpty());
        verify(festivoJpaRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("esFestivo: retorna true cuando la fecha existe en BD")
    void should_ReturnTrue_when_IsHoliday() {
        LocalDate fecha = LocalDate.of(2026, 1, 1);
        FestivoEntity entity = new FestivoEntity();
        entity.setDate(fecha);
        entity.setCountryCode("CO");

        when(festivoJpaRepository.findByDateAndCountryCode(fecha, "CO")).thenReturn(Optional.of(entity));

        assertTrue(festivoCacheService.esFestivo(fecha));
        verify(festivoJpaRepository, never()).findByYear(anyInt());
    }

    @Test
    @DisplayName("esFestivo: retorna false cuando no es festivo y no hay cache")
    void should_ReturnFalse_when_NotHoliday() {
        LocalDate fecha = LocalDate.of(2026, 7, 20);

        when(festivoJpaRepository.findByDateAndCountryCode(fecha, "CO")).thenReturn(Optional.empty());
        when(festivoJpaRepository.findByYear(2026)).thenReturn(List.of());
        when(nagerDateClient.obtenerFestivos(2026, "CO")).thenReturn(List.of());

        assertFalse(festivoCacheService.esFestivo(fecha));
    }

    @Test
    @DisplayName("esFestivo: carga festivos si no existe y luego verifica")
    void should_LoadAndCheck_when_NotInCache() {
        LocalDate fecha = LocalDate.of(2026, 7, 20);

        // Primera llamada: no existe en BD
        when(festivoJpaRepository.findByDateAndCountryCode(fecha, "CO")).thenReturn(Optional.empty());
        // Carga desde API (cache vacio)
        when(festivoJpaRepository.findByYear(2026)).thenReturn(List.of());
        when(nagerDateClient.obtenerFestivos(2026, "CO")).thenReturn(List.of());

        // Segunda llamada despues de cargar: tampoco existe (no es festivo)
        assertFalse(festivoCacheService.esFestivo(fecha));
    }
}
