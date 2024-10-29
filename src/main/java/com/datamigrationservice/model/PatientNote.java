package com.datamigrationservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "patient_note")
public class PatientNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime lastModifiedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", foreignKey = @ForeignKey(name = "fkpat_note_to_created_user"))
    private CompanyUser createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_user_id", foreignKey = @ForeignKey(name = "fk_pat_note_to_modifyed_user"))
    private CompanyUser lastModifiedByUser;

    @Column(length = 4000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", foreignKey = @ForeignKey(name = "fk_pat_note_to_patient"))
    private PatientProfile patient;
}
