package org.peter.hospital.controller;

import lombok.RequiredArgsConstructor;
import org.peter.hospital.dto.PatientResponse;
import org.peter.hospital.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<PatientResponse> getPatients(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false ,defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> doctorIds
    ) {
        return new ResponseEntity<>(patientService.getPatients(page, size, search, doctorIds), HttpStatus.OK);
    }

}
