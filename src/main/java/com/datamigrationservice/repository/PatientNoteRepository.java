package com.datamigrationservice.repository;

import com.datamigrationservice.model.PatientNote;
import com.datamigrationservice.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {

    @Query("SELECT pn FROM PatientNote pn " +
            "JOIN FETCH pn.createdByUser " +
            "JOIN FETCH pn.lastModifiedByUser " +
            "WHERE pn.patient = :patientProfile")
    List<PatientNote> findAllByPatient(PatientProfile patientProfile);
}
