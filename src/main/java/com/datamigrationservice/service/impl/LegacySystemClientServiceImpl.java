package com.datamigrationservice.service.impl;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.ClientNotesRequestDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.service.LegacySystemClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LegacySystemClientServiceImpl implements LegacySystemClientService {

    private final RestTemplate restTemplate;

    @Value("legacy.url")
    private String legacyURL;

    @Override
    public List<ClientDto> getAllClients() {
        return restTemplate.exchange(
                legacyURL + "/clients",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<List<ClientDto>>() {}
        ).getBody();
    }

    @Override
    public List<NoteDto> getClientNotes(ClientNotesRequestDto clientNotesRequestDto) {
        return restTemplate.exchange(
                legacyURL + "/notes",
                HttpMethod.POST,
                new HttpEntity<>(clientNotesRequestDto),
                new ParameterizedTypeReference<List<NoteDto>>() {}
        ).getBody();
    }
}
