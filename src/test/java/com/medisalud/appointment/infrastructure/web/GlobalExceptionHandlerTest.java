package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ConflictException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException retorna 400 con detalles de validacion")
    void should_Return400_when_ValidationError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "nombre", "must not be null");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<?> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("HttpMessageNotReadableException retorna 400")
    void should_Return400_when_HttpMessageNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("JSON parse error");

        ResponseEntity<?> response = handler.handleNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("ConstraintViolationException retorna 400")
    void should_Return400_when_ConstraintViolation() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);

        when(path.toString()).thenReturn("nombre");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");
        when(violation.getInvalidValue()).thenReturn(null);
        when((ConstraintDescriptor<jakarta.validation.constraints.NotNull>) violation.getConstraintDescriptor())
                .thenReturn((ConstraintDescriptor<jakarta.validation.constraints.NotNull>) descriptor);
        when(descriptor.getAnnotation()).thenReturn(new jakarta.validation.constraints.NotNull() {
            @Override
            public Class<? extends jakarta.validation.constraints.NotNull> annotationType() {
                return jakarta.validation.constraints.NotNull.class;
            }
            @Override
            public String message() { return "{jakarta.validation.constraints.NotNull.message}"; }
            @Override
            public Class<?>[] groups() { return new Class<?>[0]; }
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
        });
        when(ex.getConstraintViolations()).thenReturn(Set.of(violation));

        ResponseEntity<?> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException retorna 400")
    void should_Return400_when_TypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("medicoId");
        when(ex.getValue()).thenReturn("invalid-uuid");

        ResponseEntity<?> response = handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("ResourceNotFoundException retorna 404")
    void should_Return404_when_ResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Medico", UUID.randomUUID());

        ResponseEntity<?> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("BusinessException retorna 422")
    void should_Return422_when_BusinessException() {
        BusinessException ex = new BusinessException("INVALID_SCHEDULE", "Horario invalido");

        ResponseEntity<?> response = handler.handleBusiness(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    @DisplayName("ConflictException retorna 409")
    void should_Return409_when_ConflictException() {
        ConflictException ex = new ConflictException("MEDICO_SLOT_CONFLICT", "Medico ocupado");

        ResponseEntity<?> response = handler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Exception generica retorna 500")
    void should_Return500_when_UnexpectedException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<?> response = handler.handleGeneral(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Exception generica con request URI null no falla")
    void should_HandleGeneralException_when_RequestURINull() {
        when(request.getRequestURI()).thenReturn(null);
        Exception ex = new RuntimeException("Error");

        ResponseEntity<?> response = handler.handleGeneral(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
