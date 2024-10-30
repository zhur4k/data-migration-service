package com.datamigrationservice.service;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.NoteDto;
import com.datamigrationservice.model.CompanyUser;
import com.datamigrationservice.model.PatientNote;
import com.datamigrationservice.model.PatientProfile;
import com.datamigrationservice.service.impl.ImportStatisticService;

import java.util.List;

public interface ImportService {

    void importData();

    void processClientData(ClientDto clientDto, ImportStatisticService statistic);

    void processClientNote(NoteDto noteDto, PatientProfile profile, List<PatientNote> patientNotes, ImportStatisticService statistic);

    boolean checkExistNotesAndProcessExisted(List<PatientNote> patientNotes, NoteDto noteDto, CompanyUser user, ImportStatisticService statistic);
}
