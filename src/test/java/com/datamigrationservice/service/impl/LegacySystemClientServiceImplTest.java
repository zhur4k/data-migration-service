package com.datamigrationservice.service.impl;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.ClientNotesRequestDto;
import com.datamigrationservice.dto.NoteDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class LegacySystemClientServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LegacySystemClientServiceImpl legacySystemClientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        legacySystemClientService = new LegacySystemClientServiceImpl(restTemplate, validator);
    }

    @Test
    void getAllClients_withValidAndInvalidClients_returnsOnlyValidClients() {
        ClientDto validClient = new ClientDto("agency", "01588E84-D45A-EB98-F47F-716073A4F1EF", "First", "Last", (short) 200,
                LocalDate.of(1999, 10, 15), LocalDateTime.now());

        ClientDto invalidClient = new ClientDto(null, "invalid-guid", "First", "Last", null,
                LocalDate.of(1999, 10, 15), LocalDateTime.now());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(validClient, invalidClient)));

        List<ClientDto> clients = legacySystemClientService.getAllClients();

        assertNotNull(clients);
        assertEquals(1, clients.size());
        assertEquals("agency", clients.get(0).agency());
    }

    @Test
    void getAllClients_restClientException_logsError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Connection error"));

        RestClientException exception = assertThrows(RestClientException.class, () -> legacySystemClientService.getAllClients());
        assertEquals("Connection error", exception.getMessage());
    }

    @Test
    void getClientNotes_withValidAndInvalidNotes_returnsOnlyValidNotes() {
        NoteDto validNote = new NoteDto("Sample comment", "20CBCEDA-3764-7F20-0BB6-4D6DD46BA9F8",
                LocalDateTime.now(), "01588E84-D45A-EB98-F47F-716073A4F1EF", LocalDateTime.now(), "user1", LocalDateTime.now());

        NoteDto invalidNote = new NoteDto(null, null, LocalDateTime.now(),
                "01588E84-D45A-EB98-F47F-716073A4F1EF", LocalDateTime.now(), "user1", LocalDateTime.now());

        ClientNotesRequestDto requestDto = new ClientNotesRequestDto("vhh4", "01588E84-D45A-EB98-F47F-716073A4F1EF", LocalDate.now().toString(), LocalDate.now().toString());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(validNote, invalidNote)));

        List<NoteDto> notes = legacySystemClientService.getClientNotes(requestDto);

        assertNotNull(notes);
        assertEquals(1, notes.size());
        assertEquals("Sample comment", notes.get(0).comments());
    }

    @Test
    void getClientNotes_restClientException_logsError() {
        ClientNotesRequestDto requestDto = new ClientNotesRequestDto("vhh4", "01588E84-D45A-EB98-F47F-716073A4F1EF", LocalDate.now().toString(), LocalDate.now().toString());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Connection error"));

        RestClientException exception = assertThrows(RestClientException.class, () -> legacySystemClientService.getClientNotes(requestDto));
        assertEquals("Connection error", exception.getMessage());
    }
}
