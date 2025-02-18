package org.peter.hospital.dto;

import java.util.List;

public record PatientResponse(
        List<PatientDTO> data,
        int count
) {
}
