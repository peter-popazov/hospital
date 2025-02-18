package org.peter.hospital.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.peter.hospital.entity.Doctor;
import org.peter.hospital.entity.Patient;
import org.peter.hospital.entity.Visit;
import org.peter.hospital.repositories.DoctorRepository;
import org.peter.hospital.repositories.PatientRepository;
import org.peter.hospital.repositories.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DataSeeder {

    private static final int NUM_DOCTORS = 100;
    private static final int NUM_PATIENTS = 1_000;
    private static final int NUM_VISITS = 2_000;

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final Random random = new Random();

    @PostConstruct
    @Transactional
    public void seedDatabase() {
        if (doctorRepository.count() == 0 && patientRepository.count() == 0 && visitRepository.count() == 0) {
            generateData();
        }
    }

    @Transactional
    public void generateData() {
        List<Doctor> doctors = new ArrayList<>(NUM_DOCTORS);
        List<Patient> patients = new ArrayList<>(NUM_PATIENTS);

        for (int i = 1; i <= 100; i++) {
            Doctor d = new Doctor();
            d.setFirstName("First" + i);
            d.setLastName("Last" + i);
            d.setTimezone("UTC");
            doctors.add(d);
        }

        for (int i = 1; i <= 1_000; i++) {
            Patient p = new Patient();
            p.setFirstName("First" + i);
            p.setLastName("Last" + i);
            patients.add(p);
        }

        doctorRepository.saveAll(doctors);
        patientRepository.saveAll(patients);

        List<Visit> visits = new ArrayList<>(NUM_VISITS);
        for (int i = 0; i < 2_000; i++) {
            Doctor doctor = doctors.get(random.nextInt(doctors.size()));
            Patient patient = patients.get(random.nextInt(patients.size()));

            ZonedDateTime startTime = ZonedDateTime.now().plusDays(random.nextInt(30)).withHour(random.nextInt(8) + 9);
            ZonedDateTime endTime = startTime.plusMinutes(30);

            long isOccupied = visitRepository.countConflictingVisits(doctor.getId(), startTime, endTime);
            if (isOccupied > 0) {
                Visit visit = new Visit();
                visit.setDoctor(doctor);
                visit.setPatient(patient);
                visit.setStartDateTime(startTime);
                visit.setEndDateTime(endTime);
                visits.add(visit);
            }
        }
        visitRepository.saveAll(visits);
    }
}
