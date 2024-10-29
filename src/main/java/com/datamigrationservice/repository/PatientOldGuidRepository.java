package com.datamigrationservice.repository;

import com.datamigrationservice.model.PatientOldGuid;
import com.datamigrationservice.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientOldGuidRepository extends JpaRepository<PatientOldGuid, Long> {

    @Query("SELECT pog.patient FROM PatientOldGuid pog WHERE pog.oldGuid = :oldGuid")
    Optional<PatientProfile> findPatientProfileByOldGuid(String oldGuid);
}
