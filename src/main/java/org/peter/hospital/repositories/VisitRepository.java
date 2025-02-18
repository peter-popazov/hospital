package org.peter.hospital.repositories;

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

    @Query("SELECT v FROM Visit v " +
            "JOIN FETCH v.doctor d " +
            "WHERE v.patient.id IN :patientIds  " +
            "AND (:doctorIds IS NULL OR d.id IN :doctorIds) " +
            "ORDER BY v.startDateTime DESC")
    List<Visit> findLastVisitsForPatients(@Param("patientIds") List<Long> patientIds,
                                          @Param("doctorIds") List<Long> doctorIds);
}
