package com.medisalud.appointment.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class NagerDateClient {

    private final RestClient restClient;

    private static final String NAGER_BASE_URL = "https://date.nager.at/api/v3";

    public NagerDateClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(NAGER_BASE_URL)
                .build();
    }

    /**
     * Obtiene la lista de festivos para un país y año desde Nager.Date API.
     * Fallback graceful: si la API falla, retorna lista vacía.
     */
    public List<FestivoDTO> obtenerFestivos(int anio, String paisCode) {
        try {
            FestivoDTO[] response = restClient.get()
                    .uri("/PublicHolidays/{anio}/{paisCode}", anio, paisCode)
                    .retrieve()
                    .body(FestivoDTO[].class);
            log.info("Nager.Date: {} festivos obtenidos para {} en {}",
                    response != null ? response.length : 0, paisCode, anio);
            return response != null ? List.of(response) : List.of();
        } catch (Exception e) {
            log.warn("Nager.Date API fallo para {}/{}: {}. Usando cache/local.", paisCode, anio, e.getMessage());
            return List.of();
        }
    }

    @Data
    public static class FestivoDTO {
        private LocalDate date;
        @JsonProperty("localName")
        private String localName;
        private String name;
        @JsonProperty("countryCode")
        private String countryCode;
        private Boolean fixed;
        private Boolean global;
        private String[] types;
        private int year;
    }
}
