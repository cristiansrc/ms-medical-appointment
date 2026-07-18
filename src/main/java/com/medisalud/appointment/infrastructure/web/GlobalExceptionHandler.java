package com.medisalud.appointment.infrastructure.web;

import com.medisalud.appointment.domain.exception.BusinessException;
import com.medisalud.appointment.domain.exception.ConflictException;
import com.medisalud.appointment.domain.exception.ResourceNotFoundException;
import com.medisalud.appointment.infrastructure.web.dto.ApiErrorDetail;
import com.medisalud.appointment.infrastructure.web.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiErrorDetail.builder()
                        .field(fe.getField())
                        .code(fe.getCode())
                        .message(fe.getDefaultMessage())
                        .rejectedValue(fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null)
                        .build())
                .toList();
        log.warn("Validation error: {} fields rejected", details.size());
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "The request contains invalid fields.", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "The request body is malformed or contains invalid data.", request, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiErrorDetail> details = ex.getConstraintViolations().stream()
                .map(cv -> ApiErrorDetail.builder()
                        .field(cv.getPropertyPath().toString())
                        .code(cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                        .message(cv.getMessage())
                        .rejectedValue(cv.getInvalidValue() != null ? cv.getInvalidValue().toString() : null)
                        .build())
                .toList();
        log.warn("Constraint violation: {} violations", details.size());
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Constraint validation failed.", request, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ApiErrorDetail detail = ApiErrorDetail.builder()
                .field(ex.getName())
                .code("TYPE_MISMATCH")
                .message("Invalid value for parameter '" + ex.getName() + "'")
                .rejectedValue(ex.getValue() != null ? ex.getValue().toString() : null)
                .build();
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "Invalid request parameter.", request, List.of(detail));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("Business rule violation: {} - {}", ex.getCode(), ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCode(),
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict: {} - {}", ex.getCode(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getCode(),
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.", request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatusCode httpStatus, String code,
                                                           String message, HttpServletRequest request,
                                                           List<ApiErrorDetail> details) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(httpStatus.value())
                .error(HttpStatus.valueOf(httpStatus.value()).getReasonPhrase())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .details(details != null && !details.isEmpty() ? details : null)
                .build();
        return ResponseEntity.status(httpStatus).body(body);
    }
}
