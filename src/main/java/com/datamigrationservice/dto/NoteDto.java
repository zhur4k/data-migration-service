package com.datamigrationservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NoteDto(

        String comments,

        @NotNull
        String guid,

        @NotNull
        LocalDateTime modifiedDateTime,

        String clientGuid,

        LocalDateTime datetime,

        @NotNull
        String loggedUser,

        @NotNull
        LocalDateTime createdDateTime
){
}
