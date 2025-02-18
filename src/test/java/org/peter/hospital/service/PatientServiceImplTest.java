package org.peter.hospital.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.peter.hospital.dto.PatientDTO;
import org.peter.hospital.dto.PatientResponse;
import org.peter.hospital.entity.Doctor;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.peter.hospital.repositories.DoctorRepository;
import org.peter.hospital.repositories.PatientRepository;
import org.peter.hospital.repositories.VisitRepository;
import org.springframework.data.domain.*;

import java.time.ZonedDateTime;
import java.util.*;

class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPatients_ReturnsValidResponse() {
        int page = 0, size = 10;
        String search = "John";
        List<Long> doctorIds = List.of(1L, 2L);

        Patient patient1 = new Patient(1L, "John", "Doe");
        Patient patient2 = new Patient(2L, "Jane", "Doe");
        Page<Patient> patientPage = new PageImpl<>(List.of(patient1, patient2));

        Doctor doctor1 = new Doctor(1L, "Smith", "Dan", "UTC+2");
        Visit visit1 = new Visit(1L, ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), patient1, doctor1);

        when(patientRepository.findPatients(PageRequest.of(page, size), search)).thenReturn(patientPage);
        when(visitRepository.findLastVisitsForPatients(List.of(1L, 2L), doctorIds)).thenReturn(List.of(visit1));
        when(doctorRepository.countPatientsForDoctorIdsIn(doctorIds))
                .thenReturn(Collections.singletonList(new Object[]{1L, 5}));

        PatientResponse response = patientService.getPatients(page, size, search, doctorIds);

        assertNotNull(response);
        assertEquals(2, response.data().size());

        PatientDTO firstPatient = response.data().get(0);
        assertEquals("John", firstPatient.firstName());
        assertEquals(1, firstPatient.lastVisits().size());
        assertEquals("Smith", firstPatient.lastVisits().get(0).doctor().firstName());
        assertEquals("Dan", firstPatient.lastVisits().get(0).doctor().lastName());
        assertEquals(5, firstPatient.lastVisits().get(0).doctor().totalPatients()); // Doctor 1 has 5 patients

        verify(patientRepository, times(1)).findPatients(any(), anyString());
        verify(visitRepository, times(1)).findLastVisitsForPatients(any(), any());
        verify(doctorRepository, times(1)).countPatientsForDoctorIdsIn(any());
    }
}
