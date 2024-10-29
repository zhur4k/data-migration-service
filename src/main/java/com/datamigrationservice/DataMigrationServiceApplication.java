package com.datamigrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataMigrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataMigrationServiceApplication.class, args);
    }

}
