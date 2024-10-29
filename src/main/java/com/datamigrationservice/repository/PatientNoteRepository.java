package com.datamigrationservice.repository;

import com.datamigrationservice.model.PatientNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {
}
