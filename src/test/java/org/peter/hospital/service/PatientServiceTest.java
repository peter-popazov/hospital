package org.peter.hospital.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter.hospital.dto.DoctorDTO;
import org.peter.hospital.dto.PatientDTO;
import org.peter.hospital.dto.PatientResponse;
import org.peter.hospital.dto.VisitDTO;
import org.peter.hospital.repository.VisitRepository;
import org.peter.hospital.utils.VisitMapper;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @InjectMocks
    private PatientServiceImpl patientService;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private VisitMapper visitMapper;

    @Test
    public void testGetPatients_ReturnsCorrectResponse() {
        int page = 0;
        int size = 10;
        String search = "john";
        List<Long> doctorIds = Arrays.asList(1L, 2L);

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{1L, "John", "Doe", Timestamp.valueOf("2024-02-22 10:00:00"),
                        Timestamp.valueOf("2024-02-22 11:00:00"), "Dr. Smith", "Williams", 5L},
                new Object[]{1L, "John", "Doe", Timestamp.valueOf("2024-02-23 09:00:00"),
                        Timestamp.valueOf("2024-02-23 10:00:00"), "Dr. Adams", "Brown", 3L},
                new Object[]{2L, "Jane", "Doe", Timestamp.valueOf("2024-02-22 12:00:00"),
                        Timestamp.valueOf("2024-02-22 13:00:00"), "Dr. Carter", "Green", 2L}
        );

        when(visitRepository.findPatientsWithDoctors(doctorIds, search, 1, size, page * size))
                .thenReturn(mockResults);

        List<PatientDTO> mockDTOs = Arrays.asList(
                new PatientDTO("John", "Doe", List.of(
                        new VisitDTO("2024-02-22T10:00:00", "2024-02-22T11:00:00", new DoctorDTO("Dr. Smith", "Williams", 5)),
                        new VisitDTO("2024-02-23T09:00:00", "2024-02-23T10:00:00", new DoctorDTO("Dr. Adams", "Brown", 3))
                )),
                new PatientDTO("Jane", "Doe", List.of(
                        new VisitDTO("2024-02-22T12:00:00", "2024-02-22T13:00:00", new DoctorDTO("Dr. Carter", "Green", 2))
                ))
        );

        when(visitMapper.mapToPatientDTOs(mockResults)).thenReturn(mockDTOs);

        PatientResponse response = patientService.getPatients(page, size, search, doctorIds);

        assertNotNull(response);
        assertEquals(2, response.count());

        PatientDTO patient1 = response.data().get(0);
        assertEquals("John", patient1.firstName());
        assertEquals("Doe", patient1.lastName());
        assertEquals(2, patient1.lastVisits().size());

        VisitDTO visit1 = patient1.lastVisits().get(0);
        assertEquals("2024-02-22T10:00:00", visit1.start());
        assertEquals("2024-02-22T11:00:00", visit1.end());
        assertEquals("Dr. Smith", visit1.doctor().firstName());
        assertEquals("Williams", visit1.doctor().lastName());
        assertEquals(5, visit1.doctor().totalPatients());

        VisitDTO visit2 = patient1.lastVisits().get(1);
        assertEquals("2024-02-23T09:00:00", visit2.start());
        assertEquals("2024-02-23T10:00:00", visit2.end());
        assertEquals("Dr. Adams", visit2.doctor().firstName());
        assertEquals("Brown", visit2.doctor().lastName());
        assertEquals(3, visit2.doctor().totalPatients());

        PatientDTO patient2 = response.data().get(1);
        assertEquals("Jane", patient2.firstName());
        assertEquals("Doe", patient2.lastName());
        assertEquals(1, patient2.lastVisits().size());

        VisitDTO visit3 = patient2.lastVisits().get(0);
        assertEquals("2024-02-22T12:00:00", visit3.start());
        assertEquals("2024-02-22T13:00:00", visit3.end());
        assertEquals("Dr. Carter", visit3.doctor().firstName());
        assertEquals("Green", visit3.doctor().lastName());
        assertEquals(2, visit3.doctor().totalPatients());
    }
}
