package com.datamigrationservice.dto;

import java.time.LocalDateTime;

public record NoteDto(

        String comments,

        String guid,

        LocalDateTime modifiedDateTime,

        String clientGuid,

        LocalDateTime datetime,

        String loggedUser,

        LocalDateTime createdDateTime
) {
}
