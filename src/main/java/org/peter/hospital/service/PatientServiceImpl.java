package org.peter.hospital.service;

import lombok.RequiredArgsConstructor;
import org.peter.hospital.dto.DoctorDTO;
import org.peter.hospital.dto.PatientDTO;
import org.peter.hospital.dto.PatientResponse;
import org.peter.hospital.dto.VisitDTO;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.peter.hospital.repositories.DoctorRepository;
import org.peter.hospital.repositories.PatientRepository;
import org.peter.hospital.repositories.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;

    @Override
    @Transactional
    public PatientResponse getPatients(int page, int size, String search, List<Long> doctorIds) {
        if (search == null || search.isEmpty()) {
            throw new IllegalArgumentException("Search cannot be null or empty");
        }
        if (doctorIds == null || doctorIds.isEmpty()) {
            throw new IllegalArgumentException("DoctorIds cannot be null or empty");
        }

        Page<Patient> patients = patientRepository.findPatients(PageRequest.of(page, size), search);

        List<Long> patientIds = patients.stream().map(Patient::getId).toList();
        List<Visit> lastPatientsVisits = visitRepository.findLastVisitsForPatients(patientIds, doctorIds);

        Map<Long, List<Visit>> lastVisitsMap = lastPatientsVisits.stream()
                .collect(Collectors.groupingBy(visit -> visit.getPatient().getId()));

        Map<Long, Integer> doctorPatientCountMap = doctorRepository.countPatientsForDoctorIdsIn(doctorIds)
                .stream().collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).intValue()
                ));

        List<PatientDTO> patientDTOs = patients.stream()
                .map(patient -> new PatientDTO(
                        patient.getFirstName(),
                        patient.getLastName(),
                        mapVisitsToDTO(lastVisitsMap.getOrDefault(patient.getId(), List.of()), doctorPatientCountMap)
                ))
                .toList();

        return new PatientResponse(patientDTOs, patientDTOs.size());
    }

    private List<VisitDTO> mapVisitsToDTO(List<Visit> visits, Map<Long, Integer> doctorPatientCountMap) {
        return visits.stream()
                .map(visit -> new VisitDTO(
                        visit.getStartDateTime().toString(),
                        visit.getEndDateTime().toString(),
                        new DoctorDTO(
                                visit.getDoctor().getFirstName(),
                                visit.getDoctor().getLastName(),
                                doctorPatientCountMap.getOrDefault(visit.getDoctor().getId(), 0)
                        )
                ))
                .toList();
    }
}
