package com.datamigrationservice.service;

import com.datamigrationservice.dto.ClientDto;
import com.datamigrationservice.dto.NoteDto;

import java.util.List;

public interface LegacySystemClientService {

    List<ClientDto> getAllClients();

    List<NoteDto> getClientNotes(String agency, String clientGuid, String dateFrom, String dateTo);
}
