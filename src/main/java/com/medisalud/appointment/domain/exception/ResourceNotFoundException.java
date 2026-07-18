package com.medisalud.appointment.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(String.format("%s with identifier '%s' not found", resourceType, identifier));
    }
}
