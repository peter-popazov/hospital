package org.peter.hospital.repository;

import org.peter.hospital.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query(value = "SELECT COUNT(v.id) FROM visits v " +
            "WHERE v.doctor_id = :doctorId " +
            "AND ((v.start_date_time BETWEEN :start AND :end) " +
            "OR (v.end_date_time BETWEEN :start AND :end))",
            nativeQuery = true)
    long countConflictingVisits(@Param("doctorId") Long doctorId,
                                @Param("start") ZonedDateTime start,
                                @Param("end") ZonedDateTime end);


    @Query(value = """
            WITH LastVisit AS (
                SELECT v.*, 
                       ROW_NUMBER() OVER (PARTITION BY v.patient_id, v.doctor_id ORDER BY v.start_date_time DESC) AS rn
                FROM visits v
                WHERE (COALESCE(:minDoctorId, -1) = -1 OR v.doctor_id IN (:doctorIds))
            ),
            DoctorPatientCount AS (
                SELECT v.doctor_id, COUNT(DISTINCT v.patient_id) AS patient_count
                FROM visits v
                GROUP BY v.doctor_id
            ),
            FilteredPatients AS (
                SELECT p.id, p.first_name, p.last_name
                FROM patients p
                WHERE (:searchTerm IS NULL OR MATCH(p.first_name) AGAINST(CONCAT(:searchTerm, '*') IN BOOLEAN MODE))
                ORDER BY p.id 
                LIMIT :limit OFFSET :offset
            )
            SELECT fp.id as patient_id,
                   fp.first_name AS patientFirstName, 
                   fp.last_name AS patientLastName, 
                   lv.start_date_time AS startTime, 
                   lv.end_date_time AS endTime, 
                   d.first_name AS doctorFirstName,
                   d.last_name AS doctorLastName,
                   dpc.patient_count AS totalPatients
            FROM FilteredPatients fp
            LEFT JOIN LastVisit lv ON lv.patient_id = fp.id AND lv.rn = 1
            LEFT JOIN DoctorPatientCount dpc ON lv.doctor_id = dpc.doctor_id
            LEFT JOIN doctors d ON lv.doctor_id = d.id;
            """,
            countQuery = """
                    SELECT COUNT(*) 
                    FROM patients p 
                    WHERE (:searchTerm IS NULL OR MATCH(p.first_name) AGAINST(CONCAT(:searchTerm, '*') IN BOOLEAN MODE))
                    """,
            nativeQuery = true)
    List<Object[]> findPatientsWithDoctors(
            @Param("doctorIds") List<Long> doctorIds,
            @Param("searchTerm") String searchTerm,
            @Param("minDoctorId") long minDoctorId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

}
