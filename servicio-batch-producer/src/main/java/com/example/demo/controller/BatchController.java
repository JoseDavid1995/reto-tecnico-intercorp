package com.example.demo.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job importarRegistrosJob;

    public BatchController(JobLauncher jobLauncher, Job importarRegistrosJob) {
        this.jobLauncher = jobLauncher;
        this.importarRegistrosJob = importarRegistrosJob;
    }

    @PostMapping("/run")
    public ResponseEntity<String> ejecutarBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("fechaInicio", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution execution = jobLauncher.run(importarRegistrosJob, params);
            return ResponseEntity.ok("Proceso Batch ejecutado con estado: " + execution.getStatus());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al ejecutar el batch: " + e.getMessage());
        }
    }
}