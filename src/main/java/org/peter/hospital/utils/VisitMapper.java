package org.peter.hospital.utils;

import org.peter.hospital.dto.DoctorDTO;
import org.peter.hospital.dto.PatientDTO;
import org.peter.hospital.dto.VisitDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VisitMapper {

    public List<PatientDTO> mapToPatientDTOs(List<Object[]> results) {
        Map<Long, PatientDTO> patients = new TreeMap<>();

        for (Object[] row : results) {
            Long patientId = (Long) row[0];
            String patientFirstName = (String) row[1];
            String patientLastName = (String) row[2];

            String startTime = row[3] != null ? row[3].toString() : null;
            String endTime = row[4] != null ? row[4].toString() : null;

            String doctorFirstName = (String) row[5];
            String doctorLastName = (String) row[6];
            int totalPatients = row[7] != null ? ((Number) row[7]).intValue() : 0;

            patients.computeIfAbsent(patientId, id -> new PatientDTO(patientFirstName, patientLastName, new ArrayList<>()));

            if (startTime != null && endTime != null) {
                DoctorDTO doctorDTO = new DoctorDTO(doctorFirstName, doctorLastName, totalPatients);
                VisitDTO visitDTO = new VisitDTO(startTime, endTime, doctorDTO);
                patients.get(patientId).lastVisits().add(visitDTO);
            }
        }

        return new ArrayList<>(patients.values());
    }
}
