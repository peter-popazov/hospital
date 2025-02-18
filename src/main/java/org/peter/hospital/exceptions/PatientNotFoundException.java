package org.peter.hospital.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class PatientNotFoundException extends EntityNotFoundException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
