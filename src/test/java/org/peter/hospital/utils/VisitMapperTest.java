package org.peter.hospital.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.peter.hospital.dto.PatientDTO;
import org.peter.hospital.dto.VisitDTO;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VisitMapperTest {

    private VisitMapper visitMapper;

    @BeforeEach
    void setUp() {
        visitMapper = new VisitMapper();
    }


    static Stream<Arguments> provideVisitData() {
        return Stream.of(
                Arguments.of(Arrays.asList(
                        new Object[]{1L, "Peter", "Popazov", "2024-02-23 10:00:00", "2024-02-23 11:00:00", "Smith", "Samuel", 5},
                        new Object[]{2L, "Kate", "Smith", "2024-02-23 14:00:00", "2024-02-23 15:00:00", "Alice", "Doe", 3},
                        new Object[]{1L, "Peter", "Popazov", "2024-02-25 09:00:00", "2024-02-25 15:30:00", "Alice", "Doe", 3}
                )),
                Arguments.of(Arrays.asList(
                        new Object[]{1L, "John", "Doe", "2024-02-23 09:00:00", "2024-02-23 10:00:00", "Alice", "Doe", 3},
                        new Object[]{3L, "Alice", "Alison", "2024-02-23 08:00:00", "2024-02-23 09:00:00", "Smith", "Samuel", 1}
                )),
                Arguments.of(List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideVisitData")
    void shouldMapToPatientDTOs(List<Object[]> mockResults) {
        List<PatientDTO> patientDTOs = visitMapper.mapToPatientDTOs(mockResults);

        if (mockResults.isEmpty()) {
            assertThat(patientDTOs).isEmpty();
        } else {
            assertThat(patientDTOs).isNotEmpty();

            for (Object[] row : mockResults) {
                Long patientId = (Long) row[0];
                String patientFirstName = (String) row[1];
                String patientLastName = (String) row[2];

                PatientDTO patientDTO = patientDTOs.stream()
                        .filter(p -> p.firstName().equals(patientFirstName) && p.lastName().equals(patientLastName))
                        .findFirst()
                        .orElse(null);

                assertThat(patientDTO).isNotNull();
                assertThat(patientDTO.firstName()).isEqualTo(patientFirstName);
                assertThat(patientDTO.lastName()).isEqualTo(patientLastName);

                if (row[3] != null && row[4] != null) {
                    assertThat(patientDTO.lastVisits()).isNotEmpty();

                    VisitDTO visitDTO = patientDTO.lastVisits().stream()
                            .filter(v -> v.start().equals(row[3].toString()) && v.end().equals(row[4].toString()))
                            .findFirst()
                            .orElse(null);

                    assertThat(visitDTO).isNotNull();
                    assertThat(visitDTO.doctor().firstName()).isEqualTo(row[5]);
                }
            }
        }
    }
}
