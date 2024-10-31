package com.datamigrationservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientDto(

        @NotNull
        String agency,

        @NotNull
        String guid,

        String firstName,

        String lastName,

        @NotNull
        Short status,

        LocalDate dob,

        @NotNull
        LocalDateTime createdDateTime
) {
}
