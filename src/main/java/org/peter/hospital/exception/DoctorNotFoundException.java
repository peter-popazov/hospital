package org.peter.hospital.exception;

import jakarta.persistence.EntityNotFoundException;

public class DoctorNotFoundException extends EntityNotFoundException {
    public DoctorNotFoundException(String message) {
        super(message);
    }
}
