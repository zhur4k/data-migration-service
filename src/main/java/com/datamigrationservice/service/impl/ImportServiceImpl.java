package com.datamigrationservice.service.impl;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.dto.mapper.ClientDtoToClientNotesRequestDtoMapper;
import com.datamigrationservice.model.CompanyUser;
import com.datamigrationservice.model.PatientNote;
import com.datamigrationservice.model.PatientOldGuid;
import com.datamigrationservice.model.PatientProfile;
import com.datamigrationservice.repository.CompanyUserRepository;
import com.datamigrationservice.repository.PatientNoteRepository;
import com.datamigrationservice.repository.PatientOldGuidRepository;
import com.datamigrationservice.repository.PatientProfileRepository;
import com.datamigrationservice.service.ImportService;
import com.datamigrationservice.service.LegacySystemClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportServiceImpl implements ImportService {

    private final CompanyUserRepository companyUserRepository;

    private final PatientOldGuidRepository patientOldGuidRepository;

    private final PatientNoteRepository patientNoteRepository;

    private final PatientProfileRepository patientProfileRepository;

    private final LegacySystemClientService legacySystemClientService;

    private final ClientDtoToClientNotesRequestDtoMapper clientDtoToClientNotesRequestDtoMapper;

    @Override
    @Scheduled(cron = "0 15 */2 * * *")
    public void importData() {
        log.info("Starting data importing process...");
        ImportStatisticService statistic = new ImportStatisticService();
        try{
            legacySystemClientService.getAllClients()
                    .forEach(client -> processClientData(client, statistic));

            statistic.logStatistics();
            log.error("Data import process completed successfully.");
        }catch(Exception e){
            log.error("Data import process failed.", e);
        }

    }

    @Override
    public void processClientData(ClientDto clientDto, ImportStatisticService statistic){
        try {
            log.info("Start process client with GUID: {}", clientDto.guid());
            PatientProfile profile;
            List<PatientNote> patientNotes;
            Optional<PatientProfile> patientProfile = patientOldGuidRepository.findPatientProfileByOldGuid(clientDto.guid());

            if(patientProfile.isPresent()){
                profile = patientProfile.get();
                if (!List.of((short) 200, (short) 210, (short) 230).contains(profile.getStatusId())) {
                    short currentStatus = patientProfile.get().getStatusId();
                    log.warn("Inactive profile for client GUID: {}. Current status is {}. Required status: 200, 210, or 230. Skipping profile creation.",
                            clientDto.guid(), currentStatus);
                    return;
                }
                patientNotes = patientNoteRepository.findAllByPatient(profile);
            }else{
                profile = new PatientProfile();
                profile.setFirstName(clientDto.firstName());
                profile.setLastName(clientDto.lastName());
                profile.setStatusId(clientDto.status());
                patientProfileRepository.save(profile);

                PatientOldGuid patientOldGuid = new PatientOldGuid();
                patientOldGuid.setOldGuid(clientDto.guid());
                patientOldGuid.setPatient(profile);
                patientOldGuidRepository.save(patientOldGuid);

                statistic.incrementProfileCreated();
                log.info("Created new PatientProfile for client with guid: {}", clientDto.guid());
                patientNotes = null;
            }
            statistic.incrementClientProcessed();
            log.error("Finished processing client with GUID: {}", clientDto.guid());

            legacySystemClientService.getClientNotes(
                            clientDtoToClientNotesRequestDtoMapper.apply(clientDto))
                    .forEach(noteDto -> processClientNote(noteDto, profile, patientNotes, statistic));

        }catch (Exception ex){
            log.error("Failed to process client with GUID: {}", clientDto.guid(), ex);
        }
    }

    @Override
    public void processClientNote(NoteDto noteDto, PatientProfile profile, List<PatientNote> patientNotes, ImportStatisticService statistic){
        CompanyUser user = companyUserRepository.findByLogin(noteDto.loggedUser())
                .orElseGet(() -> {
                    CompanyUser companyUser = new CompanyUser();
                    companyUser.setLogin(noteDto.loggedUser());
                    companyUserRepository.save(companyUser);
                    log.info("Created new user with login: {}", noteDto.loggedUser());
                    return companyUser;
                });
        if(checkExistNotesAndProcessExisted(patientNotes, noteDto, user, statistic)){
            log.info("Note with GUID {} already exists and is up-to-date.", noteDto.guid());
            return;
        }

        PatientNote patientNote = new PatientNote();
        patientNote.setCreatedDateTime(noteDto.createdDateTime());
        patientNote.setLastModifiedDateTime(noteDto.modifiedDateTime());
        patientNote.setCreatedByUser(user);
        patientNote.setLastModifiedByUser(user);
        patientNote.setNote(noteDto.comments());
        patientNote.setOldGuid(noteDto.guid());
        patientNote.setPatient(profile);
        patientNoteRepository.save(patientNote);

        statistic.incrementNoteCreated();
        log.info("Saved new note with GUID {}", noteDto.guid());
    }

    @Override
    public boolean checkExistNotesAndProcessExisted(List<PatientNote> patientNotes, NoteDto noteDto, CompanyUser user, ImportStatisticService statistic) {
        for (PatientNote patientNote : patientNotes) {
            if (patientNote.getOldGuid().equals(noteDto.guid())) {
                if (patientNote.getLastModifiedDateTime().equals(noteDto.modifiedDateTime()))
                {
                    log.info("Note with GUID {} is already up-to-date for user {}", noteDto.guid(), user.getLogin());
                    return true;
                }
                if (patientNote.getLastModifiedDateTime().isAfter(noteDto.modifiedDateTime()))
                {
                    log.info("Skipping update for note with GUID {} as it has a more recent modification date than the source.", noteDto.guid());
                    return true;
                }
                patientNote.setLastModifiedDateTime(noteDto.modifiedDateTime());
                patientNote.setLastModifiedByUser(user);
                patientNote.setNote(noteDto.comments());
                patientNoteRepository.save(patientNote);

                statistic.incrementNoteCreated();
                log.info("Updated existing note with GUID {}", noteDto.guid());
                return true;
            }
        }
        log.info("Note with GUID {} does not exist for the patient and will be created.", noteDto.guid());
        return false;
    }
}
