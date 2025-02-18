package org.peter.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateVisitRequest(

        @NotNull(message = "Start time is required")
        String start,

        @NotNull(message = "End time is required")
        String end,

        @NotNull(message = "Patient ID is required")
        long patientId,

        @NotNull(message = "Doctor ID is required")
        long doctorId
) {
}