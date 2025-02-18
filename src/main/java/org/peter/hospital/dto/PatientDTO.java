package org.peter.hospital.dto;

import java.util.List;

public record PatientDTO(
        String firstName,
        String lastName,
        List<VisitDTO> lastVisits
) {
}
