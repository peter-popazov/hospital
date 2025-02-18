package org.peter.hospital.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class DoctorNotFoundException extends EntityNotFoundException {
    public DoctorNotFoundException(String message) {
        super(message);
    }
}
