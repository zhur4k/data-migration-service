package com.datamigrationservice.repository;

import com.datamigrationservice.model.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {
}
