package org.peter.hospital.dto;

public record DoctorDTO(
        String firstName,
        String lastName,
        int totalPatients
) {
}
