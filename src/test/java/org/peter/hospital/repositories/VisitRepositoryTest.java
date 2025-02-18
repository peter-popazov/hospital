package org.peter.hospital.repositories;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.peter.hospital.entity.Doctor;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VisitRepositoryTest {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private EntityManager entityManager;

    private Doctor doctor;
    private Patient patient;
    private Visit visit1, visit2, visit3;

    @BeforeEach
    void setup() {
        doctor = new Doctor();
        doctor.setFirstName("Gregory");
        doctor.setLastName("House");

        patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");

        entityManager.persist(doctor);
        entityManager.persist(patient);
        entityManager.flush();

        visit1 = new Visit();
        visit1.setDoctor(doctor);
        visit1.setPatient(patient);
        visit1.setStartDateTime(ZonedDateTime.now());
        visit1.setEndDateTime(ZonedDateTime.now().plusHours(1));

        visit2 = new Visit();
        visit2.setDoctor(doctor);
        visit2.setPatient(patient);
        visit2.setStartDateTime(ZonedDateTime.now().plusHours(2));
        visit2.setEndDateTime(ZonedDateTime.now().plusHours(3));

        visit3 = new Visit();
        visit3.setDoctor(doctor);
        visit3.setPatient(patient);
        visit3.setStartDateTime(ZonedDateTime.now().plusHours(4));
        visit3.setEndDateTime(ZonedDateTime.now().plusHours(5));

        entityManager.persist(visit1);
        entityManager.persist(visit2);
        entityManager.persist(visit3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldCountConflictingVisitsForDoctor() {
        // given overlapping time range
        ZonedDateTime newVisitStart = visit1.getStartDateTime().plusMinutes(30);
        ZonedDateTime newVisitEnd = visit1.getEndDateTime().plusMinutes(30);

        long conflicts = visitRepository.countConflictingVisits(doctor.getId(), newVisitStart, newVisitEnd);

        assertEquals(1, conflicts);
    }

    @Test
    void shouldFindLastVisitsForPatients() {
        List<Visit> visits = visitRepository.findLastVisitsForPatients(
                List.of(patient.getId()), List.of(doctor.getId()));

        assertEquals(3, visits.size());
        assertEquals(visit3.getId(), visits.get(0).getId());
        assertEquals(visit2.getId(), visits.get(1).getId());
        assertEquals(visit1.getId(), visits.get(2).getId());
    }
}
