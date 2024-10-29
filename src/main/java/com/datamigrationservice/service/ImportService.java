package com.datamigrationservice.service;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.model.CompanyUser;
import com.datamigrationservice.model.PatientNote;
import com.datamigrationservice.model.PatientProfile;

import java.util.List;

public interface ImportService {

    void importData();

    void processClientData(ClientDto clientDto);

    PatientProfile getOrCreatePatientProfile(ClientDto clientDto);

    void processClientNotes(NoteDto noteDto, PatientProfile profile, List<PatientNote> patientNotes);

    boolean checkExistNotesAndProcessExisted(List<PatientNote> patientNotes, NoteDto noteDto, CompanyUser user);
}
