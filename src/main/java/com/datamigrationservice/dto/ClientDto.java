package com.datamigrationservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientDto(

        String agency,

        String guid,

        String firstName,

        String lastName,

        Short status,

        LocalDate dob,

        LocalDateTime createdDateTime
) {
}
