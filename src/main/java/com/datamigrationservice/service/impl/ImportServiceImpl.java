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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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

        legacySystemClientService.getAllClients()
                .forEach(clientDto -> {
                    Optional<PatientProfile> patientProfile = patientOldGuidRepository.findPatientProfileByOldGuid(clientDto.guid());
                    PatientProfile profile;
                    if(patientProfile.isPresent()){
                        profile = patientProfile.get();
                        if(!List.of((short) 200,(short) 210,(short) 230).contains(profile.getStatusId())){
                            return;
                        }
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
                    }
                    List<PatientNote> patientNotes = patientNoteRepository.findAllByPatient(profile);
                    legacySystemClientService.getClientNotes(clientDtoToClientNotesRequestDtoMapper.apply(clientDto))
                            .forEach(noteDto -> {
                                CompanyUser user = companyUserRepository.findByLogin(noteDto.loggedUser())
                                        .orElseGet(() -> {
                                                CompanyUser companyUser = new CompanyUser();
                                                companyUser.setLogin(noteDto.loggedUser());
                                                companyUserRepository.save(companyUser);
                                                return companyUser;
                                });
                                for (PatientNote patientNote : patientNotes) {
                                    if (patientNote.getOldGuid().equals(noteDto.guid())) {
                                        if (patientNote.getLastModifiedByUser().getLogin().equals(noteDto.loggedUser())
                                                && patientNote.getLastModifiedByUser().getLogin().equals(noteDto.loggedUser())) {
                                            return;
                                        }
                                        patientNote.setLastModifiedDateTime(noteDto.modifiedDateTime());
                                        patientNote.setLastModifiedByUser(user);
                                        patientNoteRepository.save(patientNote);
                                        return;
                                    }
                                }

                                PatientNote patientNote = new PatientNote();
                                patientNote.setCreatedDateTime(LocalDateTime.now());
                                patientNote.setLastModifiedDateTime(LocalDateTime.now());
                                patientNote.setCreatedByUser(user);
                                patientNote.setLastModifiedByUser(user);
                                patientNote.setNote(noteDto.comments());
                                patientNote.setOldGuid(noteDto.guid());
                                patientNote.setPatient(profile);
                                patientNoteRepository.save(patientNote);

                            });
                    savePatientProfile(clientDto);
                });
    }

    @Override
    public void savePatientProfile(ClientDto clientDto) {

    }

    @Override
    public void savePatientNotes(List<NoteDto> noteDtos, PatientProfile patientProfile) {

    }
}
