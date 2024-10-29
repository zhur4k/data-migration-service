package com.datamigrationservice.service;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.model.PatientProfile;

import java.util.List;

public interface ImportService {

    void importData();

    void savePatientProfile(ClientDto clientDto);

    void savePatientNotes(List<NoteDto> noteDtos, PatientProfile patientProfile);
}
