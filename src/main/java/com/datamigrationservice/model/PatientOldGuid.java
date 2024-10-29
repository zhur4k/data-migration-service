package com.datamigrationservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "patient_old_guid")
public class PatientOldGuid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oldGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    private PatientProfile patient;
}
