package org.peter.hospital.repositories;

import jakarta.persistence.EntityManager;
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
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void shouldCountDistinctPatientsPerDoctor() {
        Doctor doctor1 = new Doctor();
        doctor1.setFirstName("John");
        Doctor doctor2 = new Doctor();
        doctor2.setFirstName("Jannette");
        entityManager.persist(doctor1);
        entityManager.persist(doctor2);

        Patient patient1 = new Patient();
        patient1.setFirstName("Peter");
        Patient patient2 = new Patient();
        patient2.setFirstName("Katy");
        entityManager.persist(patient1);
        entityManager.persist(patient2);

        Visit visit1 = new Visit();
        visit1.setDoctor(doctor1);
        visit1.setPatient(patient1);
        visit1.setStartDateTime(ZonedDateTime.now());
        visit1.setEndDateTime(ZonedDateTime.now().plusHours(1));
        entityManager.persist(visit1);

        Visit visit2 = new Visit();
        visit2.setDoctor(doctor1);
        visit2.setPatient(patient2);
        visit2.setStartDateTime(ZonedDateTime.now().plusDays(1));
        visit2.setEndDateTime(ZonedDateTime.now().plusDays(1).plusHours(1));
        entityManager.persist(visit2);

        Visit visit3 = new Visit();
        visit3.setDoctor(doctor2);
        visit3.setPatient(patient1);
        visit3.setStartDateTime(ZonedDateTime.now());
        visit3.setEndDateTime(ZonedDateTime.now().plusHours(1));
        entityManager.persist(visit3);

        entityManager.flush();

        List<Object[]> results = doctorRepository.countPatientsForDoctorIdsIn(List.of(doctor1.getId(), doctor2.getId()));

        Map<Long, Integer> resultMap = results.stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).intValue()));

        assertEquals(2, resultMap.get(doctor1.getId()));
        assertEquals(1, resultMap.get(doctor2.getId()));
    }
}
