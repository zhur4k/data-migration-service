package com.datamigrationservice.service.impl;

import com.datamigrationservice.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ImportStatisticService implements StatisticService {

    private int totalClientProcessed = 0;

    private int totalProfileCreated = 0;

    private int totalProfileUpdated = 0;

    private int totalNoteProcessed = 0;

    private int totalNoteCreated = 0;

    private int totalNoteUpdated = 0;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public void incrementClientProcessed() {
        totalClientProcessed++;
    }

    public void incrementProfileCreated() {
        totalProfileCreated++;
    }

    public void incrementProfileUpdated() {
        totalProfileUpdated++;
    }

    public void incrementNoteProcessed() {
        totalNoteProcessed++;
    }

    public void incrementNoteCreated() {
        totalNoteCreated++;
    }

    public void incrementNoteUpdated() {
        totalNoteUpdated++;
    }


    @Override
    public void start() {
        startAt = LocalDateTime.now();
    }

    @Override
    public void end() {
        endAt = LocalDateTime.now();
    }

    @Override
    public void  logStatistics(){
        log.info("""
                        Import statics:
                        Clients processed: {},
                        Profile created: {},
                        Profile updated: {},
                        Note processed: {},
                        Note created: {},
                        NOte updated: {},
                        Start at: {},
                        End at: {}
                        Lead time: {}""",
                totalClientProcessed,
                totalProfileCreated,
                totalProfileUpdated,
                totalNoteProcessed,
                totalNoteCreated,
                totalNoteUpdated,
                startAt,
                endAt,
                Duration.between(startAt, endAt).getSeconds()
        );
    }

    public ImportStatisticService() {
        this.startAt = LocalDateTime.now();
    }
}
