package org.peter.hospital.service;

import lombok.RequiredArgsConstructor;
import org.peter.hospital.dto.CreateVisitRequest;
import org.peter.hospital.entity.Doctor;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.peter.hospital.exception.DoctorNotFoundException;
import org.peter.hospital.exception.PatientNotFoundException;
import org.peter.hospital.repository.DoctorRepository;
import org.peter.hospital.repository.PatientRepository;
import org.peter.hospital.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public Visit createVisit(CreateVisitRequest visitRequest) {
        Patient patient = patientRepository.findById(visitRequest.patientId()).
                orElseThrow(() -> new PatientNotFoundException("Patient not found with id %d".formatted(visitRequest.patientId())));
        Doctor doctor = doctorRepository.findById(visitRequest.doctorId()).
                orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id %d".formatted(visitRequest.doctorId())));

        // it is mandatory to pass time in request in the doctor's time zone
        ZonedDateTime startTime = ZonedDateTime.parse(visitRequest.start());
        ZonedDateTime endTime = ZonedDateTime.parse(visitRequest.end());

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        long conflicts = visitRepository.countConflictingVisits(doctor.getId(), startTime, endTime);
        if (conflicts > 0) {
            throw new IllegalArgumentException("Conflicting visits found");
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setStartDateTime(startTime);
        visit.setEndDateTime(endTime);
        return visitRepository.save(visit);
    }
}
