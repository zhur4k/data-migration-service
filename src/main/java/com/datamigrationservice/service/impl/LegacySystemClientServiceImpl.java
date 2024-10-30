package com.datamigrationservice.service.impl;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.ClientNotesRequestDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.service.LegacySystemClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacySystemClientServiceImpl implements LegacySystemClientService {

    private final RestTemplate restTemplate;

    @Value("${legacy.url}")
    private String legacyURL;

    @Override
    public List<ClientDto> getAllClients() {
        log.info("Starting client data import from legacy system at URL: {}", legacyURL + "/clients");
        try {
            List<ClientDto> clients = restTemplate.exchange(
                    legacyURL + "/clients",
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<List<ClientDto>>() {}
            ).getBody();

            if (clients != null) {
                log.info("Successfully imported {} clients from legacy system.", clients.size());
            } else {
                log.warn("Client data import returned null response from legacy system.");
            }
            return clients;

        } catch (RestClientException e) {
            log.error("Failed to import client data from legacy system at URL: {}", legacyURL + "/clients", e);
            throw e;
        }
    }

    @Override
    public List<NoteDto> getClientNotes(ClientNotesRequestDto clientNotesRequestDto) {
        log.info("Starting note import for client GUID {} from legacy system.", clientNotesRequestDto.clientGuid());
        try {
            List<NoteDto> notes = restTemplate.exchange(
                    legacyURL + "/notes",
                    HttpMethod.POST,
                    new HttpEntity<>(clientNotesRequestDto),
                    new ParameterizedTypeReference<List<NoteDto>>() {}
            ).getBody();

            if (notes != null) {
                log.info("Successfully imported {} notes for client GUID {}.", notes.size(), clientNotesRequestDto.clientGuid());
            } else {
                log.warn("Note import returned null response from legacy system for client GUID {}.", clientNotesRequestDto.clientGuid());
            }
            return notes;

        } catch (RestClientException e) {
            log.error("Failed to import notes for client GUID {} from legacy system.", clientNotesRequestDto.clientGuid(), e);
            throw e;
        }
    }
}
