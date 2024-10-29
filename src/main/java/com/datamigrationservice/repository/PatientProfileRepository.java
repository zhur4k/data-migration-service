package com.datamigrationservice.repository;

import com.datamigrationservice.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
}
