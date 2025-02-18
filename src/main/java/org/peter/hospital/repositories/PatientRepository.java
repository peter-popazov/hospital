package org.peter.hospital.repositories;

import org.peter.hospital.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query(value = "SELECT * FROM patients p WHERE MATCH(p.first_name) AGAINST(CONCAT(:search, '*') IN BOOLEAN MODE)",
            countQuery = "SELECT COUNT(*) FROM patients p WHERE MATCH(p.first_name) AGAINST(CONCAT(:search, '*') IN BOOLEAN MODE)",
            nativeQuery = true)
    Page<Patient> findPatients(Pageable pageable, @Param("search") String search);

}