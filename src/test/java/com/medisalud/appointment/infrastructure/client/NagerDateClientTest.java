package com.medisalud.appointment.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NagerDateClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    private NagerDateClient client;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        client = new NagerDateClient(restClientBuilder);
    }

    @Test
    @DisplayName("obtenerFestivos: retorna lista cuando API responde exitosamente")
    void should_FetchHolidays_when_ApiResponds() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), anyInt(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        NagerDateClient.FestivoDTO dto = new NagerDateClient.FestivoDTO();
        dto.setDate(java.time.LocalDate.of(2026, 1, 1));
        dto.setLocalName("Año Nuevo");
        dto.setName("New Year");
        when(responseSpec.body(NagerDateClient.FestivoDTO[].class))
                .thenReturn(new NagerDateClient.FestivoDTO[]{dto});

        List<NagerDateClient.FestivoDTO> result = client.obtenerFestivos(2026, "CO");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Año Nuevo", result.get(0).getLocalName());
    }

    @Test
    @DisplayName("obtenerFestivos: retorna lista vacia cuando la API falla")
    void should_ReturnEmptyList_when_ApiFails() {
        when(restClient.get()).thenThrow(new RuntimeException("API connection failed"));

        List<NagerDateClient.FestivoDTO> result = client.obtenerFestivos(2026, "CO");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("obtenerFestivos: retorna lista vacia cuando API retorna null")
    void should_ReturnEmptyList_when_ApiReturnsNull() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), anyInt(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NagerDateClient.FestivoDTO[].class)).thenReturn(null);

        List<NagerDateClient.FestivoDTO> result = client.obtenerFestivos(2026, "CO");

        assertTrue(result.isEmpty());
    }
}
