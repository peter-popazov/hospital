package org.peter.hospital.service;

import org.peter.hospital.dto.CreateVisitRequest;
import org.peter.hospital.entity.Visit;
import org.springframework.transaction.annotation.Transactional;

public interface VisitService {
    @Transactional
    Visit createVisit(CreateVisitRequest visitRequest);
}
