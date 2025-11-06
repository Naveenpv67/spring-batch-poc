package com.example.customeronboarding.controller;

import com.example.customeronboarding.dto.OnboardingResponse;
import com.example.customeronboarding.dto.OnboardingStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final JobExplorer jobExplorer;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<OnboardingResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new OnboardingResponse(null, "File is empty"));
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the file
            File storedFile = new File(uploadDir + File.separator + file.getOriginalFilename());
            file.transferTo(storedFile);

            // Launch the batch job
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("fileName", storedFile.getAbsolutePath())
                    .addLong("time", System.currentTimeMillis()) // To ensure job is unique
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            return ResponseEntity.ok(new OnboardingResponse(jobExecution.getJobId(), "Job started successfully!"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new OnboardingResponse(null, "Failed to start job: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<OnboardingStatusResponse> getJobStatus(@PathVariable Long jobId) {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
        if (jobExecution == null) {
            return ResponseEntity.notFound().build();
        }

        String details = jobExecution.getStepExecutions().stream()
                .map(stepExecution -> String.format("Step %s: Status=%s, Read=%d, Write=%d, Skip=%d",
                        stepExecution.getStepName(),
                        stepExecution.getStatus(),
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getSkipCount()))
                .findFirst()
                .orElse("No step executions found.");

        return ResponseEntity.ok(new OnboardingStatusResponse(
                jobId,
                jobExecution.getStatus().toString(),
                Objects.toString(jobExecution.getExitStatus().getExitCode(), ""),
                details
        ));
    }
}
