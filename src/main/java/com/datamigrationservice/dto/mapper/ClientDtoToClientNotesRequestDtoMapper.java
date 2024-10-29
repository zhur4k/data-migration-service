package com.datamigrationservice.dto.mapper;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.ClientNotesRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Function;

@Component
public class ClientDtoToClientNotesRequestDtoMapper implements Function<ClientDto, ClientNotesRequestDto> {
    @Override
    public ClientNotesRequestDto apply(ClientDto clientDto) {
        return new ClientNotesRequestDto(
                clientDto.agency(),
                clientDto.guid(),
                clientDto.createdDateTime().toString(),
                LocalDateTime.now().toString()
        );
    }
}
