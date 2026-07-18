package com.medisalud.appointment.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super("Resource not found");
    }
}
