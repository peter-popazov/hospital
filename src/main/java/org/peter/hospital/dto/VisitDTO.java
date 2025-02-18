package org.peter.hospital.dto;

public record VisitDTO(
        String start,
        String end,
        DoctorDTO doctor
) {
}
