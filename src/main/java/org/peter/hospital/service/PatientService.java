package org.peter.hospital.service;

import org.peter.hospital.dto.PatientResponse;

import java.util.List;

public interface PatientService {
    PatientResponse getPatients(int page, int size, String search, List<Long> doctorIds);
}
