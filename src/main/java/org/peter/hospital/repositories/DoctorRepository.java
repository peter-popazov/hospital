package org.peter.hospital.repositories;

import org.peter.hospital.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query(value = "SELECT v.doctor_id, COUNT(DISTINCT v.patient_id) " +
            "FROM visits v " +
            "WHERE v.doctor_id IN (:doctorIds) " +
            "GROUP BY v.doctor_id ", nativeQuery = true)
    List<Object[]> countPatientsForDoctorIdsIn(@Param("doctorIds") List<Long> doctorIds);
}
