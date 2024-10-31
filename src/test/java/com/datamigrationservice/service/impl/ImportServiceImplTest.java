package com.datamigrationservice.service.impl;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.model.CompanyUser;
import com.datamigrationservice.model.PatientNote;
import com.datamigrationservice.model.PatientOldGuid;
import com.datamigrationservice.model.PatientProfile;
import com.datamigrationservice.repository.CompanyUserRepository;
import com.datamigrationservice.repository.PatientNoteRepository;
import com.datamigrationservice.repository.PatientOldGuidRepository;
import com.datamigrationservice.repository.PatientProfileRepository;
import com.datamigrationservice.service.LegacySystemClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportServiceImplTest {

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private PatientOldGuidRepository patientOldGuidRepository;

    @Mock
    private PatientNoteRepository patientNoteRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private LegacySystemClientService legacySystemClientService;

    @InjectMocks
    private ImportServiceImpl importService;

    private ImportStatisticService statistic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        statistic = new ImportStatisticService();
    }

    @Test
    void processClientData_createsNewPatientProfile() {
        ClientDto clientDto = new ClientDto("hgt", "GUID123", "First", "Last", (short) 200, null,null);

        when(patientOldGuidRepository.findPatientProfileByOldGuid(clientDto.guid())).thenReturn(Optional.empty());

        importService.processClientData(clientDto, statistic);

        verify(patientProfileRepository, times(1)).save(any(PatientProfile.class));
        verify(patientNoteRepository, never()).findAllByPatient(any());
    }

    @Test
    void processClientData_inactiveProfile_skipsProcessing() {
        ClientDto clientDto = new ClientDto("hgt", "GUID123", "First", "Last", (short) 200, null,null);
        PatientProfile inactiveProfile = new PatientProfile();
        inactiveProfile.setStatusId((short) 100);

        when(patientOldGuidRepository.findPatientProfileByOldGuid(clientDto.guid()))
                .thenReturn(Optional.of(inactiveProfile));

        importService.processClientData(clientDto, statistic);

        verify(patientNoteRepository, never()).findAllByPatient(any());
        verify(legacySystemClientService, never()).getClientNotes(any());
    }

    @Test
    void processClientData_createsNewProfile_savesToRepository() {
        ClientDto clientDto = new ClientDto("hgt", "GUID123", "First", "Last", (short) 200, null,null);

        when(patientOldGuidRepository.findPatientProfileByOldGuid(clientDto.guid()))
                .thenReturn(Optional.empty());

        importService.processClientData(clientDto, statistic);

        verify(patientProfileRepository, times(1)).save(any(PatientProfile.class));
        verify(patientOldGuidRepository, times(1)).save(any(PatientOldGuid.class));
    }

    @Test
    void processClientNote_newUser_createsAndSavesUser() {
        NoteDto noteDto = new NoteDto("Some comment","fsdsf-55hg5g-fhfgh", null, "user123", null, "login", LocalDateTime.now());
        PatientProfile profile = new PatientProfile();
        List<PatientNote> patientNotes = Collections.emptyList();

        when(companyUserRepository.findByLogin(noteDto.loggedUser())).thenReturn(Optional.empty());

        importService.processClientNote(noteDto, profile, patientNotes, statistic);

        verify(companyUserRepository, times(1)).save(any(CompanyUser.class));
        verify(patientNoteRepository, times(1)).save(any(PatientNote.class));
    }

    @Test
    void processClientNote_existingNote_doesNotSaveNote() {
        NoteDto noteDto = new NoteDto("Some comment","fsdsf-55hg5g-fhfgh", LocalDateTime.now(), "user123", LocalDateTime.now(), "login", LocalDateTime.now());
        CompanyUser user = new CompanyUser();
        user.setLogin("user123");

        PatientProfile profile = new PatientProfile();
        PatientNote existingNote = new PatientNote();
        existingNote.setOldGuid("fsdsf-55hg5g-fhfgh");
        existingNote.setLastModifiedDateTime(noteDto.modifiedDateTime());

        List<PatientNote> patientNotes = List.of(existingNote);

        when(companyUserRepository.findByLogin(noteDto.loggedUser())).thenReturn(Optional.of(user));

        importService.processClientNote(noteDto, profile, patientNotes, statistic);

        verify(patientNoteRepository, never()).save(any(PatientNote.class));
    }

    @Test
    void checkExistNotesAndProcessExisted_existingNoteUpdated_savesUpdatedNote() {
        NoteDto noteDto = new NoteDto("Some comment","fsdsf-55hg5g-fhfgh", LocalDateTime.now(), "user123", LocalDateTime.now(), "login", LocalDateTime.now());
        CompanyUser user = new CompanyUser();
        user.setLogin("user123");

        PatientNote existingNote = new PatientNote();
        existingNote.setOldGuid("fsdsf-55hg5g-fhfgh");
        existingNote.setLastModifiedDateTime(noteDto.modifiedDateTime().minusDays(1));

        boolean result = importService.checkExistNotesAndProcessExisted(List.of(existingNote), noteDto, user, statistic);

        assertTrue(result);
        verify(patientNoteRepository, times(1)).save(existingNote);
    }

    @Test
    void processClientNote_updatesExistingNoteIfModified() {
        NoteDto noteDto = new NoteDto("Some comment","fsdsf-55hg5g-fhfgh", LocalDateTime.now(), "user123", LocalDateTime.now(), "login", LocalDateTime.now());
        CompanyUser user = new CompanyUser();
        user.setLogin("user123");

        PatientNote existingNote = new PatientNote();
        existingNote.setOldGuid("fsdsf-55hg5g-fhfgh");
        existingNote.setLastModifiedDateTime(noteDto.modifiedDateTime().minusDays(1));

        when(companyUserRepository.findByLogin(noteDto.loggedUser()))
                .thenReturn(Optional.of(user));

        boolean result = importService.checkExistNotesAndProcessExisted(List.of(existingNote), noteDto, user, statistic);

        assertTrue(result);
        verify(patientNoteRepository, times(1)).save(existingNote);
    }

    @Test
    void checkExistNotesAndProcessExisted_noteUpToDate_doesNotSaveNote() {
        NoteDto noteDto = new NoteDto("Some comment","fsdsf-55hg5g-fhfgh", LocalDateTime.now(), "user123", LocalDateTime.now(), "login", LocalDateTime.now());
        CompanyUser user = new CompanyUser();
        user.setLogin("user123");

        PatientNote existingNote = new PatientNote();
        existingNote.setOldGuid("fsdsf-55hg5g-fhfgh");
        existingNote.setLastModifiedDateTime(noteDto.modifiedDateTime());

        List<PatientNote> patientNotes = List.of(existingNote);

        boolean result = importService.checkExistNotesAndProcessExisted(patientNotes, noteDto, user, statistic);

        assertTrue(result);
        verify(patientNoteRepository, never()).save(any(PatientNote.class));
    }
}
