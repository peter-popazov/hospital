package org.peter.hospital.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.peter.hospital.entity.Doctor;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/hospital_test",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.username=root",
        "spring.datasource.password=peter12345"
})
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

        entityManager.createNativeQuery("DROP INDEX ft_index_fn ON patients").executeUpdate();
        entityManager.createNativeQuery("CREATE FULLTEXT INDEX ft_index_fn ON patients(first_name)").executeUpdate();
    }

    @AfterEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
        // Prevent rollback
    void cleanup() {
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE visits").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE patients").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE doctors").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
        entityManager.flush();
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
    public void shouldReturnCorrectFields() {
        List<Object[]> results = visitRepository.findPatientsWithDoctors(
                null, null, -1, 10, 0
        );

        assertNotNull(results);
        assertFalse(results.isEmpty());

        for (Object[] row : results) {
            assertEquals(8, row.length);

            assertInstanceOf(Long.class, row[0]);
            assertInstanceOf(String.class, row[1]);
            assertInstanceOf(String.class, row[2]);
            assertInstanceOf(Timestamp.class, row[3]);
            assertInstanceOf(Timestamp.class, row[4]);
            assertInstanceOf(String.class, row[5]);
            assertInstanceOf(String.class, row[6]);
            assertInstanceOf(Long.class, row[7]);
        }
    }

    @Test
    void shouldReturnAllPatientsAndDoctors_whenSearchAndDoctorIdsAreEmpty() {
        List<Object[]> results = visitRepository.findPatientsWithDoctors(
                null, null, -1, 10, 0
        );

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void shouldReturnFilteredResults_whenUsingSearchTerm() {
        List<Object[]> results = visitRepository.findPatientsWithDoctors(
                null, "jo", -1, 10, 0
        );

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("John", results.get(0)[1]);
    }
}
