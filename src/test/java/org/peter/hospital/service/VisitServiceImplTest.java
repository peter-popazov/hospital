package org.peter.hospital.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter.hospital.dto.CreateVisitRequest;
import org.peter.hospital.entity.Doctor;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.peter.hospital.exceptions.DoctorNotFoundException;
import org.peter.hospital.exceptions.PatientNotFoundException;
import org.peter.hospital.repositories.DoctorRepository;
import org.peter.hospital.repositories.PatientRepository;
import org.peter.hospital.repositories.VisitRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @InjectMocks
    private VisitServiceImpl visitService;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private Patient patient;
    private Doctor doctor;
    private CreateVisitRequest visitRequest;

    @BeforeEach
    void setup() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");

        doctor = new Doctor();
        doctor.setId(2L);
        doctor.setFirstName("House");

        visitRequest = new CreateVisitRequest(
                ZonedDateTime.now().toString(),
                ZonedDateTime.now().plusHours(1).toString(),
                patient.getId(),
                doctor.getId()
        );
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> visitService.createVisit(visitRequest));

        verify(patientRepository).findById(patient.getId());
        verifyNoInteractions(doctorRepository, visitRepository);
    }

    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> visitService.createVisit(visitRequest));

        verify(patientRepository).findById(patient.getId());
        verify(doctorRepository).findById(doctor.getId());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsAfterEndTime() {
        CreateVisitRequest invalidRequest = new CreateVisitRequest(
                ZonedDateTime.now().plusHours(3).toString(),
                ZonedDateTime.now().plusHours(2).toString(),
                patient.getId(),
                doctor.getId()
        );
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));

        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(invalidRequest));
    }

    @Test
    void shouldThrowExceptionWhenVisitConflictsExist() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(visitRepository.countConflictingVisits(anyLong(), any(), any())).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(
                        new CreateVisitRequest(
                                ZonedDateTime.parse(visitRequest.start()).plusMinutes(30).toString(),
                                ZonedDateTime.parse(visitRequest.start()).plusMinutes(60).toString(),
                                visitRequest.patientId(),
                                visitRequest.doctorId())
                )
        );

        verify(visitRepository).countConflictingVisits(anyLong(), any(), any());
    }

    @Test
    void shouldCreateVisitSuccessfully() {
        Visit visit = new Visit();
        visit.setId(10L);
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setStartDateTime(ZonedDateTime.parse(visitRequest.start()));
        visit.setEndDateTime(ZonedDateTime.parse(visitRequest.end()));

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(visitRepository.countConflictingVisits(anyLong(), any(), any())).thenReturn(0L);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        Visit createdVisit = visitService.createVisit(visitRequest);

        assertNotNull(createdVisit);
        assertEquals(visit.getId(), createdVisit.getId());
        assertEquals(visit.getPatient().getId(), createdVisit.getPatient().getId());
        assertEquals(visit.getDoctor().getId(), createdVisit.getDoctor().getId());
    }
}




