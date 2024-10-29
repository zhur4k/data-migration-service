package com.datamigrationservice.dto;

public record ClientNotesRequestDto(
        String agency,
        String clientGuid,
        String dateFrom,
        String dateTo
) {
}
