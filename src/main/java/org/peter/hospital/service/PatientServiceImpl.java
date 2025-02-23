package org.peter.hospital.service;

import lombok.RequiredArgsConstructor;
import org.peter.hospital.dto.*;
import org.peter.hospital.repository.VisitRepository;
import org.peter.hospital.utils.VisitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;

    @Override
    @Transactional
    public PatientResponse getPatients(int page, int size, String search, List<Long> doctorIds) {

        // this check returns -1 if no doctorIds are passed, which is then used in SQL query to output all doctors.
        // the problem was that "COALESCE(:doctorIds, -1)" does not allow list e.g. (1,2,3) and "(:doctorIds) IS NULL" neither
        long minDoctorId = (doctorIds == null || search.isEmpty()) ? -1L
                : doctorIds.stream().min(Long::compareTo).orElse(-1L);

        List<Object[]> results = visitRepository.findPatientsWithDoctors(
                doctorIds, search, minDoctorId, size, page * size);

        List<PatientDTO> patientDTOs = visitMapper.mapToPatientDTOs(results);

        return new PatientResponse(patientDTOs, patientDTOs.size());

    }
}
