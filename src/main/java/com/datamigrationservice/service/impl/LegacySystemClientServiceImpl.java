package com.datamigrationservice.service.impl;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.ClientNotesRequestDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.service.LegacySystemClientService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacySystemClientServiceImpl implements LegacySystemClientService {

    private final RestTemplate restTemplate;

    private final Validator validator;

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
                clients = filterValidClients(clients);
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

    private List<ClientDto> filterValidClients(List<ClientDto> clients) {
        return clients.stream()
                .filter(client -> {
                    Set<ConstraintViolation<ClientDto>> violations = validator.validate(client);
                    if (!violations.isEmpty()) {
                        String errors = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        log.warn("Validation failed for ClientDto with GUID {}: {}", client.guid(), errors);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
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
                notes = filterValidNotes(notes);
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

    private List<NoteDto> filterValidNotes(List<NoteDto> notes) {
        return notes.stream()
                .filter(note -> {
                    Set<ConstraintViolation<NoteDto>> violations = validator.validate(note);
                    if (!violations.isEmpty()) {
                        String errors = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        log.warn("Validation failed for NoteDto with GUID {}: {}", note.guid(), errors);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
